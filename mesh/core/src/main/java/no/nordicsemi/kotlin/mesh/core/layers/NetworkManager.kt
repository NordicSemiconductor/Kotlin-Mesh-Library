@file:Suppress("unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE")

package no.nordicsemi.kotlin.mesh.core.layers

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import no.nordicsemi.kotlin.mesh.bearer.PduType
import no.nordicsemi.kotlin.mesh.bearer.Transmitter
import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager
import no.nordicsemi.kotlin.mesh.core.ProxyFilterEventHandler
import no.nordicsemi.kotlin.mesh.core.layers.access.Busy
import no.nordicsemi.kotlin.mesh.core.layers.network.NetworkLayer
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.MeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.UnacknowledgedMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.proxy.ProxyConfigurationMessage
import no.nordicsemi.kotlin.mesh.core.model.Address
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.Element
import no.nordicsemi.kotlin.mesh.core.model.MeshAddress
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.logger.Logger
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration
import kotlin.time.DurationUnit

/**
 * Network Manager contains the different layers of the mesh network architecture.
 *
 * @property manager Mesh network manager
 * @constructor Constructs the network manager.
 */
internal class NetworkManager(private val manager: MeshNetworkManager) {
    lateinit var proxy: ProxyFilterEventHandler

    var logger: Logger? = null
    val networkPropertiesStorage = manager.networkProperties
    private var networkLayer = NetworkLayer(this)
    // var lowerTransportLayer = LowerTransportLayer(this)
    // var upperTransportLayer = UpperTransportLayer(this)
    // var accessLayer = AccessLayer(this)

    var transmitter: Transmitter? = manager.transmitter

    var meshNetwork = manager.meshNetwork.replayCache.first()

    var networkParameters = NetworkParameters()
    private val mutex = Mutex(locked = true)

    private var outgoingMessages = mutableSetOf<MeshAddress>()

    /**
     * Handles the received PDU of a given type.
     *
     * @param incomingPdu Incoming PDU.
     * @param type        PDU type.
     */
    suspend fun handle(incomingPdu: ByteArray, type: PduType) {
        networkLayer.handle(incomingPdu = incomingPdu, type = type)
    }

    /**
     * Publishes the given message using the Publish information from the given Model. If
     * publication is not set, this message does nothing.
     *
     * If publication retransmission is set, this method will retransmit the message specified
     * number of times, if applicable keeps the same TID value.
     *
     * @param message     Message to be published.
     * @param from        Source model from which the message is originating from.
     */
    fun publish(message: MeshMessage, from: Model) {
        val publish = from.publish ?: return
        val localElement = from.parentElement ?: return
        val applicationKey = meshNetwork.applicationKeys[publish.index]

        // calculate the TTL to be used
        val ttl = when (publish.ttl != 0xFF) {
            true -> publish.ttl.toUByte()
            false -> localElement.parentNode?.defaultTTL?.toUByte() ?: networkParameters.defaultTtl
        }

        //TODO accessLayer.send(message, localElement, publish.address, ttl, applicationKey, retransmit false)

        if (message is AcknowledgedMeshMessage) {
            var count = publish.retransmit.count.toInt()
            if (count > 0) {
                val interval: Duration = publish.retransmit.interval
                fixedRateTimer(
                    daemon = false,
                    period = interval.toLong(DurationUnit.MILLISECONDS)
                ) {
                    if (--count > 0) {
                        // TODO accessLayer.send(message, localElement, publish.address, ttl, applicationKey, retransmit = true)
                    } else cancel()
                }
            }
        }
    }

    @Throws(Busy::class)
    private suspend fun ensureNotBusy(destination: MeshAddress) = mutex.withLock {
        require(!outgoingMessages.contains(destination)) { throw Busy }
        outgoingMessages.add(destination)
        false
    }

    suspend fun send(
        message: MeshMessage,
        element: Element,
        destination: MeshAddress,
        initialTtl: UByte?,
        key: ApplicationKey
    ) {
        try {
            require(!ensureNotBusy(destination = destination)) { return }
            // TODO setDeliveryCallback
            // TODO accessLayer.send(message, element, destination, initialTtl, key, retransmit = false)
        } catch (e: Exception) {
            cancel(
                handler = MessageHandle(
                    message = message,
                    source = element.unicastAddress,
                    destination = destination,
                    manager = this@NetworkManager
                )
            )
        }
    }

    suspend fun send(
        message: AcknowledgedMeshMessage,
        element: Element,
        destination: Address,
        initialTtl: UByte?,
        key: ApplicationKey
    ) {
        val meshAddress = MeshAddress.create(address = destination)
        try {
            require(!ensureNotBusy(destination = meshAddress)) { return }
            // TODO setDeliveryCallback
            // TODO accessLayer.send(message, element, destination, initialTtl, key, retransmit = true)
        } catch (e: Exception) {
            cancel(
                handler = MessageHandle(
                    message = message,
                    source = element.unicastAddress,
                    destination = meshAddress,
                    manager = this@NetworkManager
                )
            )
        }
    }

    suspend fun send(
        message: UnacknowledgedMeshMessage,
        element: Element,
        destination: Address,
        initialTtl: UByte?
    ) {
        val meshAddress = MeshAddress.create(address = destination)
        try {
            require(!ensureNotBusy(destination = meshAddress)) { return }
            // TODO setDeliveryCallback
            // TODO accessLayer.send(message, element, destination, initialTtl, key, retransmit = true)
        } catch (e: Exception) {
            cancel(
                handler = MessageHandle(
                    message = message,
                    source = element.unicastAddress,
                    destination = meshAddress,
                    manager = this@NetworkManager
                )
            )
        }
    }

    /**
     * Encrypts the message with the Device Key and the first Network Key known to it, and sends to
     * the given destination address.
     *
     * The [ConfigNetKeyDelete] will be signed with a different network key than its removing.
     *
     * This method does not send nor return PDUs to be sent. Instead, for each created segment it
     * calls transmitter's ``Transmitter/send(_:ofType:)`` method, which should send the PDU over
     * the air. This is in order to support retransmission in case a packet was lost and needs to be
     * sent again after block acknowledgment was received.
     *
     * @param configMessage   Config message to be sent.
     * @param element         Source Element from which the message is originating from.
     * @param destination     Destination address.
     * @param ttl             Initial TTL (Time To Live) value of the message. If `nil`, default
     *                        Node TTL will be used.
     */
    suspend fun send(
        configMessage: AcknowledgedMeshMessage,
        element: Element,
        destination: Address,
        ttl: UByte?
    ) {
        val meshAddress = MeshAddress.create(address = destination)
        try {
            require(!ensureNotBusy(destination = meshAddress)) { return }
            // TODO setDeliveryCallback
            // TODO accessLayer.send(message, element, destination, initialTtl, key, retransmit = true)

        } catch (e: Exception) {
            cancel(
                handler = MessageHandle(
                    message = configMessage,
                    source = element.unicastAddress,
                    destination = meshAddress,
                    manager = this@NetworkManager
                )
            )
        }
    }

    /**
     * Sends the Proxy Configuration message to the connected Proxy node.
     *
     * @param message Proxy Configuration message to be sent.
     */
    suspend fun send(message: ProxyConfigurationMessage) {
        networkLayer.send(message)
    }

    fun cancel(handler: MessageHandle) {
        // TODO accessLayer.cancel(handler)
    }
}