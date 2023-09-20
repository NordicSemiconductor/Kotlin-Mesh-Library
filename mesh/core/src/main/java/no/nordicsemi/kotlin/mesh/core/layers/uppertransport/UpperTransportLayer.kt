@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.layers.uppertransport

import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import no.nordicsemi.kotlin.mesh.core.layers.KeySet
import no.nordicsemi.kotlin.mesh.core.layers.NetworkManager
import no.nordicsemi.kotlin.mesh.core.layers.access.AccessPdu
import no.nordicsemi.kotlin.mesh.core.layers.lowertransport.AccessMessage
import no.nordicsemi.kotlin.mesh.core.layers.lowertransport.ControlMessage
import no.nordicsemi.kotlin.mesh.core.layers.lowertransport.LowerTransportPdu
import no.nordicsemi.kotlin.mesh.core.layers.network.LowerTransportPduType
import no.nordicsemi.kotlin.mesh.core.model.Address
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.get
import no.nordicsemi.kotlin.mesh.logger.LogCategory
import java.util.Timer
import kotlin.concurrent.timer
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Defines the behaviour of the Upper Transport Layer of the Mesh Networking Stack.
 */
internal class UpperTransportLayer(private val networkManager: NetworkManager) {

    private val meshNetwork = networkManager.meshNetwork
    private val logger = networkManager.logger
    private val queue: MutableMap<Address, MutableList<MessageData>> = mutableMapOf()
    private val mutex = Mutex(locked = true)
    private var heartbeatPublisher: Timer? = null

    /**
     * Handles a received Lower Transport Pdu.
     *
     * Depending on the PDU type, the message will be either propagated to Access Layer or handled
     * internally.
     * @param lowerTransportPdu The received Lower Transport PDU.
     */
    fun handle(lowerTransportPdu: LowerTransportPdu) {
        when (lowerTransportPdu.type) {
            LowerTransportPduType.ACCESS_MESSAGE -> {
                val accessMessage = lowerTransportPdu as AccessMessage
                val pair = UpperTransportPdu.decode(
                    message = accessMessage, network = meshNetwork
                )
                pair?.let {
                    logger?.i(LogCategory.UPPER_TRANSPORT) { "Received ${it.first} received." }
                } ?: logger?.w(LogCategory.UPPER_TRANSPORT) { "Failed to decode PDU" }
            }

            LowerTransportPduType.CONTROL_MESSAGE -> {
                val message = lowerTransportPdu as ControlMessage
                when (message.opCode) {
                    HeartbeatMessage.OP_CODE -> {
                        HeartbeatMessage.init(message = message)?.let { heartbeat ->
                            logger?.i(LogCategory.UPPER_TRANSPORT) {
                                "$heartbeat received from ${message.source.toHex(prefix0x = true)}"
                            }
                        }
                    }

                    else -> {
                        logger?.i(LogCategory.UPPER_TRANSPORT) {
                            "Unsupported Control Message received (opCode: ${message.opCode})"
                        }
                    }
                }
            }
        }
    }

    /**
     * Encrypts the Access PDU using given key set and sends it down to the Lower Transport Layer.
     *
     * @param accessPdu  Access PDU to be sent.
     * @param ttl        Initial TTL value of the message. If 'null', default Node TTL will be used.
     * @param keySet     Key set to be used to encrypt the message.
     */
    suspend fun send(accessPdu: AccessPdu, ttl: UByte?, keySet: KeySet) {
        // Get the current sequence number for the given source Element's address.
        val sequence = networkManager.networkLayer.nextSequenceNumber(
            address = UnicastAddress(accessPdu.source)
        )
        val pdu = UpperTransportPdu.init(
            pdu = accessPdu,
            keySet = keySet,
            sequence = sequence,
            ivIndex = meshNetwork.ivIndex
        )
        logger?.i(LogCategory.UPPER_TRANSPORT) {
            "Sending $pdu encrypted using key: $keySet"
        }
        if (pdu.transportPdu.size > 15 || accessPdu.isSegmented) {
            // Enqueue the PDU. If the queue was empty, the PDU will be sent immediately.
            enqueue(pdu, ttl, keySet.networkKey)
        } else {
            networkManager.lowerTransportLayer.send(
                pdu = pdu,
                initialTtl = ttl,
                networkKey = keySet.networkKey
            )
        }
    }

    /**
     * Returns whether the underlying layer is in progress of receiving a message from the given
     * address.
     *
     * @param address Source address.
     * @return 'true' if some, but not all packets of a segmented message were received from the
     *         given source address; 'false' if not packets were received or the message was
     *         complete before calling this method.
     */
    fun isReceivingResponse(address: Address): Boolean {
        return networkManager.lowerTransportLayer.isReceivingMessage(address)
    }

    suspend fun onLowerTransportLayerSent(destination: Address) {
        mutex.withLock {
            require(queue[destination] != null) {
                return
            }
            // Remove the PDU that has just been sent.
            queue[destination]?.removeFirst()
        }

        // Try to send the next one
        sendNext(destination)
    }

    /**
     * Invalidates and optionally restarts the periodic Heartbeat publisher if Heartbeat publication
     * has been set.
     */
    fun refreshHeartbeatPublisher() {
        heartbeatPublisher?.let {
            logger?.i(LogCategory.UPPER_TRANSPORT) {
                "Publishing periodic Heartbeat messages cancelled"
            }
            it.cancel()
        }

        meshNetwork.localProvisioner?.node?.heartbeatPublication?.takeIf {
            it.isPeriodicHeartbeatStateEnabled
        }?.let { heartbeatPublication ->
            heartbeatPublication.state?.let {
                logger?.i(LogCategory.UPPER_TRANSPORT) {
                    "Publishing periodic Heartbeat messages initiated."
                }
                val interval = heartbeatPublication.period.toInt()
                    .toDuration(DurationUnit.SECONDS)
                heartbeatPublisher = timer(
                    name = "HeartbeatPublisher",
                    period = interval.inWholeMilliseconds
                ) {
                    val layer = this@UpperTransportLayer
                    // Check if the local node still exists.
                    val localNode = requireNotNull(meshNetwork.localProvisioner?.node) {
                        layer.heartbeatPublisher?.cancel()
                        layer.heartbeatPublisher?.purge()
                        logger?.i(LogCategory.UPPER_TRANSPORT) {
                            "Publishing periodic Heartbeat messages cancelled."
                        }
                        return@timer
                    }
                    // Check if the network key exists.
                    val networkKey = requireNotNull(
                        localNode.networkKeys.get(
                            heartbeatPublication.index
                        )
                    ) {
                        layer.heartbeatPublisher?.cancel()
                        layer.heartbeatPublisher?.purge()
                        logger?.i(LogCategory.UPPER_TRANSPORT) {
                            "Publishing periodic Heartbeat messages cancelled."
                        }
                        return@timer
                    }

                    val state = requireNotNull(
                        heartbeatPublication.state
                    ) {
                        layer.heartbeatPublisher?.cancel()
                        layer.heartbeatPublisher?.purge()
                        logger?.i(LogCategory.UPPER_TRANSPORT) {
                            "Publishing periodic Heartbeat messages cancelled."
                        }
                        return@timer
                    }
                    require(heartbeatPublication.isPeriodicHeartbeatStateEnabled) {
                        layer.heartbeatPublisher?.cancel()
                        layer.heartbeatPublisher?.purge()
                        logger?.i(LogCategory.UPPER_TRANSPORT) {
                            "Publishing periodic Heartbeat messages cancelled."
                        }
                        return@timer
                    }
                    val heartbeat = HeartbeatMessage.init(
                        heartbeatPublication = heartbeatPublication,
                        source = localNode.primaryUnicastAddress,
                        destination = heartbeatPublication.address,
                        ivIndex = meshNetwork.ivIndex,
                    )

                    networkManager.scope.launch {
                        send(heartbeat, networkKey)
                    }
                    // If the last periodic Heartbeat message has been sent, cancel the timer.
                    if (!state.shouldSendMorePeriodicHeartbeatMessages()) {
                        layer.heartbeatPublisher?.cancel()
                        layer.heartbeatPublisher?.purge()
                        logger?.i(LogCategory.UPPER_TRANSPORT) {
                            "Publishing periodic Heartbeat messages finished."
                        }
                        return@timer
                    }
                    // Do nothing. Timer will be fired again.
                }
            }
        }
    }

    private suspend fun enqueue(
        pdu: UpperTransportPdu,
        initialTtl: UByte?,
        networkKey: NetworkKey
    ) {
        val destination = pdu.destination.address
        var count: Int
        mutex.withLock {
            queue[destination] = queue[destination] ?: mutableListOf()
            queue[destination]!!.add(MessageData(pdu, initialTtl, networkKey))
            count = queue[destination]!!.size
        }

        if (count == 1) sendNext(destination)
    }

    /**
     * Sends the next enqueued PDU. This method does nothing if the queue for the given destination
     * is empty or does not exist.
     */
    private suspend fun sendNext(destination: Address) {
        val messageData = requireNotNull(mutex.withLock {
            queue[destination]?.firstOrNull()
        }) {
            return
        }
        // If another PDU has been enqueued, send it.
        networkManager.lowerTransportLayer.send(
            pdu = messageData.pdu,
            initialTtl = messageData.ttl,
            networkKey = messageData.networkKey
        )
    }

    /**
     * Handles received Heartbeat message. If the local Node has active subscription matching
     * received Heartbeat, the count value will be incremented.
     *
     * @param heartbeat Received Heartbeat message.
     */
    private fun handle(heartbeat: HeartbeatMessage) {
        meshNetwork.localProvisioner?.node?.heartbeatSubscription?.updateIfMatches(heartbeat)
    }

    /**
     * Sends a Heartbeat message.
     *
     * @param heartbeat   Heartbeat message to be sent.
     * @param networkKey  Network key to be used to encrypt the message.
     */
    private suspend fun send(heartbeat: HeartbeatMessage, networkKey: NetworkKey) {
        logger?.i(LogCategory.UPPER_TRANSPORT) {
            "Sending $heartbeat to ${heartbeat.destination.toHex(prefix0x = true)}" + "encrypted " +
                    "using key: $networkKey"
        }
        networkManager.lowerTransportLayer.send(heartbeat = heartbeat, networkKey = networkKey)
    }
}

/**
 * Message data class containing the pdu, ttl and network key.
 *
 * @property pdu            Pdu to be sent.
 * @property ttl            TTL value of the pdu.
 * @property networkKey     Network key to be used to encrypt the pdu.
 * @constructor Creates a message data object.
 */
internal data class MessageData(
    var pdu: UpperTransportPdu,
    val ttl: UByte?,
    val networkKey: NetworkKey
)