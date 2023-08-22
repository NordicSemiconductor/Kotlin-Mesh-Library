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
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.MeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.UnacknowledgedConfigMessage
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
internal class NetworkManager internal constructor(private val manager: MeshNetworkManager) {
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

    /**
     * Ensures that the local node is not busy sending a message to the given destination address.
     *
     * @param destination Destination address.
     * @return `true` if the node is busy sending a message to the given destination address,
     * @throws Busy if the node is busy sending a message to the given destination address.
     */
    @Throws(Busy::class)
    private suspend fun ensureNotBusy(destination: MeshAddress) = mutex.withLock {
        require(!outgoingMessages.contains(destination)) { throw Busy }
        outgoingMessages.add(destination)
        false
    }

    /**
     * Encrypts the message with the Application Key and a Network Key bound to it, and sends to the
     * given destination address.
     *
     * This method does not send nor return PDUs to be sent. Instead, for each created segment it
     * calls transmitter's [Transmitter.send] method, which should send the PDU over the air. This
     * is in order to support retransmission in case a packet was lost and needs to be sent again
     * after block acknowledgment was received.
     *
     * @param message          Message to be sent.
     * @param element          Source Element.
     * @param destination      Destination address.
     * @param initialTtl       Initial TTL (Time To Live) value of the message. If `nil`, the
     *                         default Node TTL will be used.
     * @param applicationKey   Application Key to sign the message.
     * @throws Busy if the node is busy sending a message to the given destination address.
     */
    @Throws(Busy::class)
    suspend fun send(
        message: MeshMessage,
        element: Element,
        destination: MeshAddress,
        initialTtl: UByte?,
        applicationKey: ApplicationKey
    ) {
        require(!ensureNotBusy(destination = destination)) { return }
        // TODO accessLayer.send(message, element, destination, initialTtl, key, retransmit = false)
        mutex.withLock { outgoingMessages.remove(destination) }
    }

    /**
     * Encrypts the message with the Application Key and a Network Key bound to it, and sends to the
     * given destination address.
     *
     * This method does not send nor return PDUs to be sent. Instead, for each created segment it
     * calls transmitter's [Transmitter.send] method, which should send the PDU over the air. This
     * is in order to support retransmission in case a packet was lost and needs to be sent again
     * after block acknowledgment was received.
     *
     * @param message         Message to be sent.
     * @param element         Source Element.
     * @param destination     Destination Unicast Address.
     * @param initialTtl      Initial TTL (Time To Live) value of the message. If `nil`, the default
     *                        Node TTL will be used.
     * @param applicationKey  Application Key to sign the message.
     * @throws Busy if the node is busy sending a message to the given destination address.
     */
    @Throws(Busy::class)
    suspend fun send(
        message: AcknowledgedMeshMessage,
        element: Element,
        destination: Address,
        initialTtl: UByte?,
        applicationKey: ApplicationKey
    ) {
        val meshAddress = MeshAddress.create(address = destination)
        require(!ensureNotBusy(destination = meshAddress)) { return }

        // TODO accessLayer.send(message, element, destination, initialTtl, key, retransmit = true)
        mutex.withLock { outgoingMessages.remove(meshAddress) }
    }

    /**
     * Encrypts the message with the Device Key and the first Network Key known to the target device,
     * and sends to the given destination address.
     *
     * This method does not send nor return PDUs to be sent. Instead, for each created segment it
     * calls transmitter's [Transmitter.send] method, which should send the PDU over the air. This
     * is in order to support retransmission in case a packet was lost and needs to be sent again
     * after block acknowledgment was received.
     *
     * @param configMessage  Message to be sent.
     * @param element        Source Element.
     * @param destination    Destination address.
     * @param initialTtl     Initial TTL (Time To Live) value of the message. If `nil`, the default
     *                       Node TTL will be used.
     * @throws Busy if the node is busy sending a message to the given destination address.
     */
    @Throws(Busy::class)
    suspend fun send(
        configMessage: UnacknowledgedConfigMessage,
        element: Element,
        destination: Address,
        initialTtl: UByte?
    ) {
        val meshAddress = MeshAddress.create(address = destination)
        require(!ensureNotBusy(destination = meshAddress)) { return }
        // TODO accessLayer.send(message, element, destination, initialTtl, key, retransmit = true)
        mutex.withLock { outgoingMessages.remove(meshAddress) }
    }

    /**
     * Encrypts the message with the Device Key and the first Network Key known to the target device,
     * and sends to the given destination address.
     *
     * The [ConfigNetKeyDelete] will be signed with a different Network Key that is removing.
     *
     * This method does not send nor return PDUs to be sent. Instead, for each created segment it
     * calls transmitter's [Transmitter.send] method, which should send the PDU over the air. This
     * is in order to support retransmission in case a packet was lost and needs to be sent again
     * after block acknowledgment was received.
     *
     * @param configMessage   Message to be sent.
     * @param element         Source Element.
     * @param destination     Destination address.
     * @param initialTtl      Initial TTL (Time To Live) value of the message. If `nil`, the default
     *                        Node TTL will be used.
     * @throws Busy if the node is busy sending a message to the given destination address.
     */
    @Throws(Busy::class)
    suspend fun send(
        configMessage: AcknowledgedConfigMessage,
        element: Element,
        destination: Address,
        initialTtl: UByte?
    ) {
        val meshAddress = MeshAddress.create(address = destination)
        require(!ensureNotBusy(destination = meshAddress)) { return }
        // TODO accessLayer.send(message, element, destination, initialTtl, key, retransmit = true)
        mutex.withLock { outgoingMessages.remove(meshAddress) }
    }

    /**
     * Sends the Proxy Configuration message to the connected Proxy node.
     *
     * @param message Proxy Configuration message to be sent.
     */
    suspend fun send(message: ProxyConfigurationMessage) {
        networkLayer.send(message = message)
    }
}