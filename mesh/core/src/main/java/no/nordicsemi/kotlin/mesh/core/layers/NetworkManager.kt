@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.layers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import no.nordicsemi.kotlin.mesh.bearer.BearerError
import no.nordicsemi.kotlin.mesh.bearer.MeshBearer
import no.nordicsemi.kotlin.mesh.bearer.Pdu
import no.nordicsemi.kotlin.mesh.bearer.PduType
import no.nordicsemi.kotlin.mesh.bearer.Transmitter
import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager
import no.nordicsemi.kotlin.mesh.core.ProxyFilterEventHandler
import no.nordicsemi.kotlin.mesh.core.layers.access.AccessLayer
import no.nordicsemi.kotlin.mesh.core.layers.access.Busy
import no.nordicsemi.kotlin.mesh.core.layers.lowertransport.LowerTransportLayer
import no.nordicsemi.kotlin.mesh.core.layers.network.NetworkLayer
import no.nordicsemi.kotlin.mesh.core.layers.uppertransport.UpperTransportLayer
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.MeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.MeshResponse
import no.nordicsemi.kotlin.mesh.core.messages.UnacknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNetKeyDelete
import no.nordicsemi.kotlin.mesh.core.messages.proxy.ProxyConfigurationMessage
import no.nordicsemi.kotlin.mesh.core.model.Address
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.Element
import no.nordicsemi.kotlin.mesh.core.model.MeshAddress
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.get
import no.nordicsemi.kotlin.mesh.logger.Logger
import kotlin.concurrent.timer
import kotlin.time.Duration
import kotlin.time.DurationUnit

/**
 * Network Manager contains the different layers of the mesh network architecture.
 *
 * @property manager Mesh network manager
 * @constructor Constructs the network manager.
 */
internal class NetworkManager internal constructor(private val manager: MeshNetworkManager) :
        NetworkManagerEventTransmitter {

    internal val scope: CoroutineScope = manager.scope
    lateinit var proxy: ProxyFilterEventHandler

    var logger: Logger? = null
    val networkPropertiesStorage = manager.networkProperties
    internal var networkLayer = NetworkLayer(this)
        private set
    internal var lowerTransportLayer = LowerTransportLayer(this)
        private set
    internal var upperTransportLayer = UpperTransportLayer(this)
        private set
    internal var accessLayer = AccessLayer(this)
        private set

    var bearer: MeshBearer? = manager.meshBearer

    val meshNetwork: MeshNetwork
        get() = manager.network!!

    var networkParameters = NetworkParameters()
    private val mutex = Mutex(locked = true)

    private var outgoingMessages = mutableSetOf<MeshAddress>()

    private val _networkManagerEventFlow = MutableSharedFlow<NetworkManagerEvent>()
    override val networkManagerEventFlow
        get() = _networkManagerEventFlow.asSharedFlow()

    override fun emitNetworkManagerEvent(event: NetworkManagerEvent) {
        _networkManagerEventFlow.tryEmit(event)
    }


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
    suspend fun publish(message: MeshMessage, from: Model) {
        val publish = from.publish ?: return
        val localElement = from.parentElement ?: return
        val applicationKey = meshNetwork.applicationKeys.get(publish.index) ?: return

        // calculate the TTL to be used
        val ttl = when (publish.ttl != 0xFF.toUByte()) {
            true -> publish.ttl
            false -> localElement.parentNode?.defaultTTL ?: networkParameters.defaultTtl
        }

        accessLayer.send(
            message = message,
            element = localElement,
            destination = publish.address as MeshAddress,
            ttl = ttl,
            applicationKey = applicationKey,
            retransmit = false
        )

        if (message is AcknowledgedMeshMessage) {
            var count = publish.retransmit.count.toInt()
            if (count > 0) {
                val interval: Duration = publish.retransmit.interval
                timer(
                    daemon = false,
                    period = interval.toLong(DurationUnit.MILLISECONDS)
                ) {
                    if (--count > 0) {
                        scope.launch {
                            accessLayer.send(
                                message = message,
                                element = localElement,
                                destination = publish.address as MeshAddress,
                                ttl = ttl,
                                applicationKey = applicationKey,
                                retransmit = true
                            )
                        }
                    } else {
                        cancel()
                    }
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
        accessLayer.send(
            message = message,
            element = element,
            destination = destination,
            ttl = initialTtl,
            applicationKey = applicationKey,
            retransmit = false
        )
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

        accessLayer.send(
            message = message,
            element = element,
            destination = meshAddress,
            ttl = initialTtl,
            applicationKey = applicationKey,
            retransmit = true
        )
        mutex.withLock { outgoingMessages.remove(meshAddress) }
    }

    /**
     * Encrypts the message with the Device Key and the first Network Key known to the target
     * device, and sends to the given destination address.
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
        accessLayer.send(
            message = configMessage,
            localElement = element,
            destination = destination,
            initialTtl = initialTtl
        )
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
        accessLayer.send(
            message = configMessage,
            localElement = element,
            destination = destination,
            initialTtl = initialTtl
        )
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

    /**
     * Replies to the received message, which was sent with the given key set, with the given
     * message.
     *
     * @param origin      Destination address of the message that the reply is for.
     * @param message     Response message to be sent.
     * @param element     Source Element.
     *
     *
     */
    suspend fun reply(
        origin: Address,
        message: MeshResponse,
        element: Element,
        destination: Address,
        keySet: KeySet
    ) {
        accessLayer.reply(
            origin = origin,
            message = message,
            element = element,
            destination = destination,
            keySet = keySet
        )
    }


    /**
     * Awaits and returns the mesh pdu received by the bearer.
     *
     * @return PDU.
     */
    internal suspend fun awaitBearerPdu(): Pdu = bearer?.pdus?.first {
        it.type == PduType.PROVISIONING_PDU
    } ?: throw BearerError.Closed
}