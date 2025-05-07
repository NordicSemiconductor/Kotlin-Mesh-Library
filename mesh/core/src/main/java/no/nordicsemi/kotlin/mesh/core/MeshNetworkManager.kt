@file:Suppress("unused", "RedundantSuspendModifier", "MemberVisibilityCanBePrivate")

package no.nordicsemi.kotlin.mesh.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import no.nordicsemi.kotlin.mesh.bearer.MeshBearer
import no.nordicsemi.kotlin.mesh.bearer.Transmitter
import no.nordicsemi.kotlin.mesh.core.exception.ImportError
import no.nordicsemi.kotlin.mesh.core.exception.NoNetwork
import no.nordicsemi.kotlin.mesh.core.layers.MessageHandle
import no.nordicsemi.kotlin.mesh.core.layers.NetworkManager
import no.nordicsemi.kotlin.mesh.core.layers.NetworkManagerEvent
import no.nordicsemi.kotlin.mesh.core.layers.access.CannotDelete
import no.nordicsemi.kotlin.mesh.core.layers.access.CannotRelay
import no.nordicsemi.kotlin.mesh.core.layers.access.InvalidDestination
import no.nordicsemi.kotlin.mesh.core.layers.access.InvalidElement
import no.nordicsemi.kotlin.mesh.core.layers.access.InvalidKey
import no.nordicsemi.kotlin.mesh.core.layers.access.InvalidSource
import no.nordicsemi.kotlin.mesh.core.layers.access.InvalidTtl
import no.nordicsemi.kotlin.mesh.core.layers.access.ModelNotBoundToAppKey
import no.nordicsemi.kotlin.mesh.core.layers.access.NoAppKeysBoundToModel
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.MeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.UnacknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.UnacknowledgedMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNetKeyDelete
import no.nordicsemi.kotlin.mesh.core.messages.proxy.ProxyConfigurationMessage
import no.nordicsemi.kotlin.mesh.core.model.Address
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.Element
import no.nordicsemi.kotlin.mesh.core.model.Group
import no.nordicsemi.kotlin.mesh.core.model.MeshAddress
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.Node
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.get
import no.nordicsemi.kotlin.mesh.core.model.serialization.MeshNetworkSerializer.deserialize
import no.nordicsemi.kotlin.mesh.core.model.serialization.MeshNetworkSerializer.serialize
import no.nordicsemi.kotlin.mesh.core.model.serialization.config.NetworkConfiguration
import no.nordicsemi.kotlin.mesh.logger.LogCategory
import no.nordicsemi.kotlin.mesh.logger.Logger
import java.util.UUID

/**
 * MeshNetworkManager is the entry point to the Mesh library.
 *
 * @param storage               Custom storage option allowing users to save the mesh network to a
 *                              custom location.
 * @param secureProperties      Custom storage option allowing users to save the sequence number.
 * @param scope                 The scope in which the mesh network will be created.
 * @property meshBearer         Mesh bearer is responsible for sending and receiving mesh messages.
 * @property logger             The logger is responsible for logging mesh messages.
 * @property networkManager     Handles the mesh networking stack.
 */
class MeshNetworkManager(
    private val storage: Storage,
    internal val secureProperties: SecurePropertiesStorage,
    internal val scope: CoroutineScope,
) {
    private val mutex by lazy { Mutex() }
    private val _meshNetwork = MutableSharedFlow<MeshNetwork>(replay = 1, extraBufferCapacity = 10)
    val meshNetwork = _meshNetwork.asSharedFlow()
    internal var network: MeshNetwork? = null
        private set

    internal var networkManager: NetworkManager? = null
        private set(value) {
            field = value
            value?.let {
                observeNetworkManagerEvents()
            }
        }

    var logger: Logger? = null

    private var meshBearer: MeshBearer? = null

    var proxyFilter: ProxyFilter
        internal set

    var localElements: List<Element>
        get() = network?.localElements ?: emptyList()
        set(value) {
            network?._localElements = value.toMutableList()
            networkManager?.accessLayer?.reinitializePublishers()
        }

    init {
        proxyFilter = ProxyFilter(scope = scope, manager = this)
    }

    fun setMeshBearerType(meshBearer: MeshBearer) {
        this.meshBearer = meshBearer
        networkManager?.setMeshBearerType(bearer = meshBearer)
    }

    /**
     * Loads the network from the storage provided by the user.
     *
     * @return true if the configuration was successfully loaded or false otherwise.
     */
    suspend fun load() = storage.load().takeIf { it.isNotEmpty() }?.let {
        val meshNetwork = deserialize(it)
        this@MeshNetworkManager.network = meshNetwork
        _meshNetwork.emit(meshNetwork)
        networkManager = NetworkManager(this)
        proxyFilter.onNewNetworkCreated()
        true
    } ?: false

    /**
     * Saves the network in the local storage provided by the user.
     */
    suspend fun save() {
        mutex.withLock {
            export()?.also {
                storage.save(it)
                this@MeshNetworkManager.network?.let { network ->
                    _meshNetwork.emit(network)
                }
            }
        }
    }

    /**
     * Creates a Mesh Network with a given name and a UUID. If a UUID is not provided a random will
     * be generated.
     *
     * @param name Name of the mesh network.
     * @param uuid 128-bit Universally Unique Identifier (UUID), which allows differentiation among
     *             multiple mesh networks.
     */
    suspend fun create(
        name: String = "Mesh Network",
        uuid: UUID = UUID.randomUUID(),
        provisionerName: String = "Mesh Provisioner",
    ) = create(name = name, uuid = uuid, provisioner = Provisioner(name = provisionerName))

    /**
     * Creates a Mesh Network with a given name and a UUID. If a UUID is not provided a random will
     * be generated.
     *
     * @param name Name of the mesh network.
     * @param uuid 128-bit UUID of the mesh network.
     * @param provisioner Provisioner to be added to the network.
     */
    suspend fun create(
        name: String = "Mesh Network",
        uuid: UUID = UUID.randomUUID(),
        provisioner: Provisioner,
    ) = MeshNetwork(uuid = uuid, _name = name).also {
        it.add(name = "Primary Network Key", index = 0u)
        it.add(provisioner)
        network = it
        networkManager = NetworkManager(this)
        _meshNetwork.emit(it)
    }

    /**
     * Imports a Mesh Network from a byte array containing a Json defined by the Mesh Configuration
     * Database profile.
     *
     * @return a mesh network configuration decoded from the given byte array.
     * @throws ImportError if deserializing fails.
     */
    @Throws(ImportError::class)
    suspend fun import(array: ByteArray) = runCatching {
        deserialize(array)
            .also {
                network = it
                networkManager = NetworkManager(this)
                proxyFilter.onNewNetworkCreated()
                _meshNetwork.emit(it)
            }
    }.getOrElse {
        if (it is ImportError) throw it
        else throw ImportError(error = "Error while deserializing the mesh network", throwable = it)
    }

    /**
     * Exports a mesh network to a Json defined by the Mesh Configuration Database Profile based
     * on the given configuration.
     *
     * @param configuration Specifies if the network should be fully exported or partially.
     * @return Bytearray containing the Mesh network configuration.
     */
    suspend fun export(
        configuration: NetworkConfiguration = NetworkConfiguration.Full,
    ) = network?.let {
        serialize(
            network = it,
            configuration = configuration
        ).toString().toByteArray()
    }

    /**
     * This method tries to publish the given message using the publication information set in the
     * [Model].
     *
     * If the retransmission is set to a value greater than 0, and the message is unacknowledged,
     * this method will retransmit it number of times with the count and interval specified in the
     * retransmission object.
     *
     * If the publication is not configured for the given Model, this method does nothing.
     *
     * Note: This method does not check whether the given Model does support the given message. It
     *       will publish whatever message is given using the publication configuration of the given
     *       Model.
     *
     * An appropriate callback of the ``MeshNetworkDelegate`` will be called when
     * the message has been sent successfully or a problem occurred.
     *
     * @param message: The message to be sent.
     * @param model:   The model from which to send the message.
     * @returns Message handle that can be used to cancel sending.
     */
    suspend fun publish(message: MeshMessage, model: Model) = networkManager?.let {
        model.let {
            val element = it.parentElement ?: return null
            it.publish?.let { publish ->
                val address = publish.address
                network?.applicationKeys?.get(index = publish.index)?.let {
                    networkManager?.let { networkManager ->
                        scope.launch {
                            networkManager.publish(message = message, from = model)
                        }
                        MessageHandle(
                            message = message,
                            source = element.unicastAddress,
                            destination = address as MeshAddress,
                            manager = networkManager
                        )
                    }
                }
            }
        }
    }

    /**
     * Encrypts the message with the Application Key and the Network Key bound to it, and sends to
     * the given destination address.
     *
     * The method completes when the message has been sent or an error occurred.
     *
     * @param message              Message to be sent.
     * @param localElement         Source Element. If `nil`, the primary Element will be used. The
     *                             Element must belong to the local Provisioner's Node.
     * @param destination          Destination address.
     * @param initialTtl           Initial TTL (Time To Live) value of the message. If `null`, the
     *                             default Node TTL will be used.
     * @param applicationKey       Application Key to sign the message.
     * @throws NoNetwork if the mesh network has not been created.
     * @throws InvalidSource if the Local Provisioner has no Unicast Address assigned.
     * @throws InvalidDestination if the Address is not a Unicast Address, an Unknown destination
     *                            Node, the Node does not have a Network Key,the Node's device key
     *                            is unknown or Cannot remove last Network Key.
     * @throws InvalidElement if the element does not belong to the local node.
     * @throws InvalidTtl if the TTL value is invalid.
     */
    @Throws(
        NoNetwork::class,
        InvalidDestination::class,
        InvalidSource::class,
        InvalidElement::class,
        InvalidTtl::class
    )
    suspend fun send(
        message: MeshMessage,
        localElement: Element?,
        destination: MeshAddress,
        initialTtl: UByte?,
        applicationKey: ApplicationKey,
    ) {
        val networkManager = requireNotNull(networkManager) {
            println("Error: Mesh Network not created.")
            throw NoNetwork
        }
        val network = requireNotNull(network) {
            println("Error: Mesh Network not created.")
            throw NoNetwork
        }
        val localNode = requireNotNull(network.localProvisioner?.node) {
            println("Error: Local Provisioner has no Unicast Address assigned.")
            throw InvalidSource
        }
        val source = localElement ?: localNode.elements.firstOrNull() ?: run {
            println("Error: Local Provisioner has no Unicast Address assigned.")
            throw InvalidSource
        }
        require(source.parentNode == localNode) {
            println("Error: The given Element does not belong to the local Node.")
            throw InvalidElement
        }
        require(initialTtl == null || initialTtl <= 127u) {
            println("Error: TTL value $initialTtl is invalid.")
            throw InvalidTtl
        }
        // A message may be sent to a local Node, or using a GATT Proxy Node. Check if the message
        // can be relayed to the destination using a Proxy Node. The Proxy Node must know the
        // Network Key; otherwise it will not be able to decode the destination and decrement TTL.

        if (destination is UnicastAddress) {
            if (!localNode.containsElementWithAddress(destination.address) &&
                proxyFilter.proxy?.knows(applicationKey.boundNetworkKey) != true
            ) {
                println(
                    "Error: The GATT Proxy Node is not connected or it cannot decrypt " +
                            "${applicationKey.boundNetworkKey}"
                )
                throw CannotRelay
            }
        } else {
            proxyFilter.proxy?.takeIf {
                !it.knows(applicationKey.boundNetworkKey)
            }?.let { proxy ->
                logger?.w(category = LogCategory.PROXY) {
                    "${proxy.name} cannot relay messages using " +
                            "${applicationKey.boundNetworkKey}, message will be sent only to " +
                            "the local Node."
                }
            } ?: logger?.w(category = LogCategory.PROXY) {
                "No GATT Proxy connected, message will be sent only to the local Node."
            }
        }
        networkManager.send(
            message = message,
            element = source,
            destination = destination,
            initialTtl = initialTtl,
            applicationKey = applicationKey
        )
    }

    /**
     * Encrypts the message with the Application Key and a Network Key bound to it, and sends to the
     * given [Group].
     *
     * The method completes when the message has been sent or an error occurred.
     *
     * @param message        Message to be sent.
     * @param localElement   Source Element. If null, the primary Element will be used. The Element
     *                       must belong to the local Provisioner's Node.
     * @param group          Target Group.
     * @param initialTtl     Initial TTL (Time To Live) value of the message. If `null`, the default
     *                       Node TTL will be used.
     * @param key            Application Key to sign the message.
     * @throws NoNetwork if the mesh network has not been created.
     * @throws InvalidSource if the Local Provisioner has no Unicast Address assigned.
     * @throws InvalidDestination if the Address is not Unicast Address, an Unknown destination Node,
     *                            the Node does not have a Network Key,the Node's device key is
     *                            unknown or Cannot remove last Network Key.
     * @throws InvalidElement if the element does not belong to the local node.
     * @throws InvalidTtl if the TTL value is invalid.
     */
    @Throws(
        NoNetwork::class,
        InvalidDestination::class,
        InvalidSource::class,
        InvalidElement::class,
        InvalidTtl::class
    )
    suspend fun send(
        message: MeshMessage,
        localElement: Element? = null,
        group: Group,
        initialTtl: UByte? = null,
        key: ApplicationKey,
    ) {
        send(
            message = message,
            localElement = localElement,
            destination = group.address as MeshAddress,
            initialTtl = initialTtl,
            applicationKey = key
        )
    }

    /**
     * Encrypts the message with the first Application Key bound to the given [Model] and the
     * Network Key bound to it, and sends it to the Node to which the Model belongs to.
     *
     * The method completes when the message has been sent or an error occurred.
     *
     * @param message        Message to be sent.
     * @param localElement   Source Element. If `nil`, the primary Element will be used. The
     *                       Element must belong to the local Provisioner's Node.
     * @param model          Destination Model.
     * @param initialTtl     Initial TTL (Time To Live) value of the message. If `nil`, the default
     *                       Node TTL will be used.
     * @throws NoNetwork if the mesh network has not been created.
     * @throws InvalidDestination if the element does not belong to a node.
     * @throws ModelNotBoundToAppKey if the model is not bound to any application key.
     */
    @Throws(
        NoNetwork::class,
        InvalidDestination::class,
        ModelNotBoundToAppKey::class,
        InvalidSource::class,
        InvalidElement::class
    )
    suspend fun send(
        message: UnacknowledgedMeshMessage,
        localElement: Element? = null,
        model: Model,
        initialTtl: UByte? = null,
        applicationKey: ApplicationKey? = null,
    ) {
        require(network != null) { throw NoNetwork }
        val node = model.parentElement?.parentNode ?: run {
            println("Error: Element does not belong to a Node")
            throw InvalidDestination
        }
        val destination = model.parentElement?.unicastAddress ?: run {
            println("Error: Element does not belong to a Node")
            throw InvalidDestination
        }

        if (applicationKey != null && !applicationKey.isBoundTo(model = model)) {
            println("Error: Model is not bound to this Application Key.")
            throw ModelNotBoundToAppKey
        }

        if (applicationKey == null && model.boundApplicationKeys.isEmpty()) {
            println("Error: Model is not bound to any Application Key.")
            throw NoAppKeysBoundToModel
        }

        val selectedAppKey = applicationKey ?: model.boundApplicationKeys
            .firstOrNull {
                node.isLocalProvisioner || proxyFilter.proxy?.knows(it.boundNetworkKey) == true
            }

        if (selectedAppKey == null || (!node.isLocalProvisioner &&
                    proxyFilter.proxy?.knows(selectedAppKey.boundNetworkKey) != true)
        ) {
            println("Error: No GATT Proxy connected or no common Network Keys")
            throw CannotRelay
        }

        send(
            message = message,
            localElement = localElement,
            destination = destination,
            initialTtl = initialTtl,
            applicationKey = selectedAppKey
        )
    }

    /**
     * Encrypts the message with the common Application Key bound to both given [Model]s and the
     * Network Key bound to it, and sends it to the Node to which the target Model belongs to.
     *
     * The method completes when the message has been sent or an error occurred.
     *
     * @param message      Message to be sent.
     * @param localModel   Source Model who's primary Element will be used.
     * @param model        Destination Model.
     * @param initialTtl   Initial TTL (Time To Live) value of the message. If `nil`, the default
     *                     Node TTL will be used.
     * @throws NoNetwork if the mesh network has not been created.
     * @throws InvalidDestination if the element does not belong to a node.
     * @throws ModelNotBoundToAppKey if the model is not bound to any application key.
     * @throws InvalidSource if the source model does not belong to an Element or if the element
     *                       does not belong to the local node.
     */
    @Throws(
        NoNetwork::class,
        InvalidDestination::class,
        ModelNotBoundToAppKey::class,
        InvalidSource::class,
        InvalidElement::class
    )
    suspend fun send(
        message: UnacknowledgedMeshMessage,
        localModel: Model,
        model: Model,
        initialTtl: UByte? = null,
        applicationKey: ApplicationKey? = null,
    ) {
        localModel.parentElement?.let {
            send(
                message = message,
                localElement = it,
                model = model,
                initialTtl = initialTtl,
                applicationKey = applicationKey
            )
        } ?: {
            println("Error: Source Model does not belong to an Element")
            throw InvalidSource
        }
    }

    /**
     * Encrypts the message with the first Application Key bound to the given [Model] and a Network
     * Key bound to it, and sends it to the Node to which the Model belongs to and returns the
     * response.
     *
     * @param message        Message to be sent.
     * @param localElement   Source Element. If `nil`, the primary Element will be used. The Element
     *                       must belong to the local Provisioner's Node.
     * @param model          Destination Model.
     * @param initialTtl     Initial TTL (Time To Live) value of the message. If `nil`, the default
     *                       Node TTL will be used.
     * @returns A Response with the expected [AcknowledgedMeshMessage.responseOpCode] received from
     *          the target Node.
     * @throws NoNetwork if the mesh network has not been created.
     * @throws InvalidDestination if the Model does not belong to an Element.
     * @throws ModelNotBoundToAppKey if the model is not bound to any application key.
     * @throws InvalidSource if the Local Provisioner has not Unicast Address assigned.
     * @throws InvalidElement if the element does not belong to the local node.
     * @throws InvalidTtl if the TTL value is invalid.
     */
    @Throws(
        NoNetwork::class,
        InvalidDestination::class,
        ModelNotBoundToAppKey::class,
        InvalidSource::class,
        InvalidElement::class,
        InvalidTtl::class
    )
    suspend fun send(
        message: AcknowledgedMeshMessage,
        localElement: Element? = null,
        model: Model,
        initialTtl: UByte? = null,
        applicationKey: ApplicationKey? = null,
    ): MeshMessage? {
        val networkManager = requireNotNull(networkManager) {
            println("Error: Mesh Network not created.")
            throw NoNetwork
        }
        val network = requireNotNull(network) {
            println("Error: Mesh Network not created.")
            throw NoNetwork
        }
        val destination = requireNotNull(model.parentElement?.unicastAddress) {
            println("Error: Model does not belong to an Element.")
            throw InvalidDestination
        }
        val node = model.parentElement?.parentNode ?: run {
            println("Error: Element does not belong to a Node")
            throw InvalidDestination
        }

        if (applicationKey != null && !applicationKey.isBoundTo(model = model)) {
            println("Error: Model is not bound to this Application Key.")
            throw ModelNotBoundToAppKey
        }

        if (applicationKey == null && model.boundApplicationKeys.isEmpty()) {
            println("Error: Model is not bound to any Application Key.")
            throw NoAppKeysBoundToModel
        }

        // Check if the application Key is known to the Proxy Node, or the message is sent to the local
        // Node.
        val selectedAppKey = applicationKey ?: model.boundApplicationKeys
            .firstOrNull {
                node.isLocalProvisioner || proxyFilter.proxy?.knows(it.boundNetworkKey) == true
            }

        if (selectedAppKey == null || (!node.isLocalProvisioner &&
                    proxyFilter.proxy?.knows(selectedAppKey.boundNetworkKey) != true)
        ) {
            println("Error: No GATT Proxy connected or no common Network Keys")
            throw CannotRelay
        }

        val localNode = requireNotNull(network.localProvisioner?.node) {
            println("Error: Local Provisioner has no Unicast Address assigned.")
            throw InvalidSource
        }
        val source = requireNotNull(localElement ?: localNode.elements.firstOrNull()) {
            println("Error: Local Provisioner has no Unicast Address assigned.")
            throw InvalidSource
        }

        require(source.parentNode == localNode) {
            println("Error: The Element does not belong to the local Node.")
            throw InvalidElement
        }
        require(initialTtl == null || initialTtl <= 127u) {
            println("Error: TTL value $initialTtl is invalid.")
            throw InvalidTtl
        }

        return networkManager.send(
            ackMessage = message,
            element = source,
            destination = destination,
            initialTtl = initialTtl,
            applicationKey = selectedAppKey
        )
    }

    /**
     * Encrypts the message with the common Application Key bound to both given [Model]s and a
     * Network Key bound to it, and sends it to the Node to which the target Model belongs to.
     *
     * @param message         Message to be sent.
     * @param localModel      Source Model who's primary element will be used.
     * @param model           Destination Model.
     * @param initialTtl      Initial TTL (Time To Live) value of the message. If `nil`, the default
     *                        Node TTL will be used.
     * @returns A response with the expected [AcknowledgedMeshMessage.responseOpCode] received from
     *          the target Node.
     * @throws NoNetwork if the mesh network has not been created.
     * @throws InvalidDestination if the Model does not belong to an Element.
     * @throws ModelNotBoundToAppKey if the model is not bound to any application key.
     * @throws InvalidSource if the source model does not belong to an Element.
     * @throws InvalidElement if the element does not belong to the local node.
     * @throws InvalidTtl if the TTL value is invalid.
     */
    @Throws(
        NoNetwork::class,
        InvalidDestination::class,
        ModelNotBoundToAppKey::class,
        InvalidSource::class,
        InvalidElement::class,
        InvalidTtl::class
    )
    suspend fun send(
        message: AcknowledgedMeshMessage,
        localModel: Model,
        model: Model,
        initialTtl: UByte?,
    ): MeshMessage = localModel.parentElement
        ?.let {
            send(message = message, localElement = it, model = model, initialTtl = initialTtl)
        } ?: run {
        println("Error: Source Model does not belong to an Element.")
        throw InvalidSource
    }

    /**
     * Sends a Configuration Message to the Node with given destination address and returns the
     * received response.
     *
     * The `destination` must be a Unicast Address, otherwise the method throws an
     * [InvalidDestination] error.
     *
     * @param message         Message to be sent.
     * @param destination     Destination Unicast Address.
     * @param initialTtl      Initial TTL (Time To Live) value of the message. If `nil`, the default
     *                        Node TTL will be used.
     * @throws NoNetwork If the mesh network has not been created.
     * @throws InvalidSource Local Node does not have configuration capabilities (no Unicast Address
     *                       assigned).
     * @throws InvalidDestination Destination address is not a Unicast Address or it belongs to an
     *                            unknown Node.
     * @throws CannotDelete When trying to delete the last Network Key on the device.
     * @returns Message handle that can be used to cancel sending.
     * @throws NoNetwork if the mesh network has not been created.
     * @throws InvalidSource if the Local Provisioner has no Unicast Address assigned.
     * @throws InvalidDestination if the Address is not Unicast Address, an Unknown destination
     *                            Node, the Node does not have a Network Key,the Node's device key
     *                            is unknown or Cannot remove last Network Key.
     * @throws InvalidTtl if the TTL value is invalid.
     */
    @OptIn(ExperimentalStdlibApi::class)
    @Throws(NoNetwork::class, InvalidSource::class, InvalidDestination::class, CannotDelete::class)
    suspend fun send(
        message: UnacknowledgedConfigMessage,
        destination: Address,
        initialTtl: UByte? = null,
        networkKey: NetworkKey? = null,
    ) {
        val networkManager = requireNotNull(networkManager) {
            println("Error: Mesh Network not created.")
            throw NoNetwork
        }
        val network = requireNotNull(network) {
            println("Error: Mesh Network not created.")
            throw NoNetwork
        }
        val element = requireNotNull(network.localProvisioner?.node?.primaryElement) {
            println("Error: Local Provisioner has no Unicast Address assigned.")
            throw InvalidSource
        }
        val dst = MeshAddress.create(address = destination)
        require(dst is UnicastAddress) {
            println("Error: Address ${destination.toHexString()} is not a Unicast Address.")
            throw InvalidDestination
        }
        val node = requireNotNull(network.node(dst)) {
            println("Error: Unknown destination Node.")
            throw InvalidDestination
        }
        require(node.netKeys.isNotEmpty()) {
            println("Fatal Error: The target Node does not have a Network Key.")
            throw InvalidDestination
        }

        if (networkKey != null && !node.knows(key = networkKey)) {
            println("Error: Node does not know the given Network Key.")
            throw InvalidKey
        }

        // Check if the application Key is known to the Proxy Node, or the message is sent to the
        // local Node.
        val selectedNetKey = networkKey ?: node.networkKeys
            .firstOrNull {
                // Unless the message is sent locally, take only keys known to the Proxy Node.
                node.isLocalProvisioner || proxyFilter.proxy?.knows(it) == true
            }

        if (selectedNetKey == null ||
            (!node.isLocalProvisioner && proxyFilter.proxy?.knows(selectedNetKey) != true)
        ) {
            println("Error: No GATT Proxy connected or no common Network Keys")
            throw CannotRelay
        }

        requireNotNull(node.deviceKey) {
            println("Fatal Error: Node's device key is unknown.")
            throw InvalidDestination
        }
        require(initialTtl == null || initialTtl <= 127u) {
            println("Error: TTL value $initialTtl is invalid.")
            throw InvalidTtl
        }
        networkManager.send(
            configMessage = message,
            element = element,
            destination = destination,
            initialTtl = initialTtl,
            networkKey = selectedNetKey
        )
    }

    /**
     * Sends a Configuration Message to the primary Element on the given [Node].
     *
     * @param message                Message to be sent.
     * @param node                   Destination Node.
     * @param initialTtl             Initial TTL (Time To Live) value of the message. If `nil`, the
     *                               default Node TTL will be used.
     * @throws CannotDelete when trying to delete the last Network Key on the device.
     * @returns Message handle that can be used to cancel sending.
     * @throws NoNetwork if the mesh network has not been created.
     * @throws InvalidSource if the Local Provisioner has no Unicast Address assigned.
     * @throws InvalidDestination if the Address is not Unicast Address, an Unknown destination
     *                            Node, the Node does not have a Network Key,the Node's device key
     *                            is unknown or Cannot remove last Network Key.
     * @throws InvalidTtl if the TTL value is invalid.
     */
    @Throws(
        NoNetwork::class,
        InvalidDestination::class,
        InvalidSource::class,
        InvalidTtl::class
    )
    suspend fun send(message: UnacknowledgedConfigMessage, node: Node, initialTtl: UByte?) = send(
        message = message,
        destination = node.primaryUnicastAddress.address,
        initialTtl = initialTtl
    )

    /**
     * Sends Configuration Message to the Node with given destination Address.
     *
     * The [destination] must be a [UnicastAddress], otherwise the method throws an
     * [InvalidDestination] error.
     *
     * @param message         Message to be sent.
     * @param destination     Destination Unicast Address.
     * @param initialTtl      Initial TTL (Time To Live) value of the message. If `nil`, the default
     *                        Node TTL will be used.
     * @throws CannotDelete   when trying to delete the last Network Key on the device.
     * @returns Response associated with the message.
     * @throws NoNetwork if the mesh network has not been created.
     * @throws InvalidSource if the Local Provisioner has no Unicast Address assigned.
     * @throws InvalidDestination if the Address is not Unicast Address, an Unknown destination
     *                            Node, the Node does not have a Network Key,the Node's device key
     *                            is unknown or Cannot remove last Network Key.
     * @throws InvalidTtl if the TTL value is invalid.
     */
    @OptIn(ExperimentalStdlibApi::class)
    @Throws(
        NoNetwork::class,
        InvalidDestination::class,
        InvalidSource::class,
        InvalidTtl::class
    )
    suspend fun send(
        message: AcknowledgedConfigMessage,
        destination: Address,
        initialTtl: UByte? = null,
        networkKey: NetworkKey? = null,
    ): MeshMessage? {
        val networkManager = requireNotNull(networkManager) {
            println("Error: Mesh Network not created.")
            throw NoNetwork
        }
        val network = requireNotNull(network) {
            println("Error: Mesh Network not created.")
            throw NoNetwork
        }
        val element = requireNotNull(network.localProvisioner?.node?.primaryElement) {
            println("Error: Local Provisioner has no Unicast Address assigned.")
            throw InvalidSource
        }
        val dst = MeshAddress.create(address = destination)
        require(dst is UnicastAddress) {
            println("Error: ${destination.toHexString()} is not a Unicast Address.")
            throw InvalidDestination
        }
        val node = requireNotNull(network.node(dst)) {
            println("Error: Unknown destination Node ${destination.toHexString()}.")
            throw InvalidDestination
        }
        require(node.netKeys.isNotEmpty()) {
            println("Fatal Error: The target Node does not have a Network Key.")
            throw InvalidDestination
        }

        if (networkKey != null && !node.knows(key = networkKey)) {
            println("Error: Node does not know the given Network Key.")
            throw InvalidKey
        }

        // Check if the application Key is known to the Proxy Node, or the message is sent to the
        // local Node.
        val configNetKeyDelete = message as? ConfigNetKeyDelete

        val selectedNetworkKey = networkKey ?: node.networkKeys.firstOrNull {
            (configNetKeyDelete?.index != it.index) &&
                    (node.isLocalProvisioner || proxyFilter.proxy?.knows(it) == true)
        }

        if (selectedNetworkKey == null ||
            (!node.isLocalProvisioner && proxyFilter.proxy?.knows(selectedNetworkKey) != true)
        ) {
            if (configNetKeyDelete != null &&
                (networkKey == null || networkKey.index == configNetKeyDelete.index)
            ) {
                println("Error: Cannot delete the last Network Key or a key used to secure the message")
                throw CannotDelete
            }
            println("Error: No GATT Proxy connected or no common Network Keys")
            throw CannotRelay
        }

        requireNotNull(node.deviceKey) {
            println("Fatal Error: Node's device key is unknown.")
            throw InvalidDestination
        }
        if (message is ConfigNetKeyDelete) {
            require(node.netKeys.size > 1) {
                println("Error: Cannot remove last Network Key.")
                throw InvalidDestination
            }
        }
        require(initialTtl == null || initialTtl <= 127u) {
            println("Error: TTL value $initialTtl is invalid.")
            throw InvalidTtl
        }
        return networkManager.send(
            configMessage = message,
            element = element,
            destination = destination,
            initialTtl = initialTtl,
            networkKey = selectedNetworkKey
        )
    }

    /**
     * Sends a Configuration Message to the primary Element on the given [Node] and returns the
     * received response.
     *
     * @param message     Message to be sent.
     * @param node        Destination Node.
     * @param initialTtl  Initial TTL (Time To Live) value of the message. If `nil`, the default
     *                    Node TTL will be used.
     * @throws CannotDelete when trying to delete the last Network Key on the device.
     * @returns Response associated with the message.
     * @throws NoNetwork if the mesh network has not been created.
     * @throws InvalidSource if the Local Provisioner has no Unicast Address assigned.
     * @throws InvalidDestination if the Address is not Unicast Address, an Unknown destination
     *                            Node, the Node does not have a Network Key,the Node's device key
     *                            is unknown or Cannot remove last Network Key.
     * @throws InvalidTtl if the TTL value is invalid.
     */
    suspend fun send(message: AcknowledgedConfigMessage, node: Node, initialTtl: UByte?) = send(
        message = message,
        destination = node.primaryUnicastAddress.address,
        initialTtl = initialTtl
    )

    /**
     * Sends the Configuration Message to the primary Element of the local [Node] and returns the
     * received response.
     *
     * @param message The acknowledged configuration message to be sent.
     * @return The response associated with the message.
     * @throws NoNetwork when the mesh network has not been created
     * @throws InvalidSource when the local Node does not have configuration capabilities.
     */
    @Throws(NoNetwork::class, InvalidSource::class)
    suspend fun sendToLocalNode(message: AcknowledgedConfigMessage): MeshMessage? {
        val network = requireNotNull(network) {
            println("Error: Mesh Network not created.")
            throw NoNetwork
        }

        val destination = requireNotNull(network.localProvisioner?.node?.primaryUnicastAddress) {
            println("Error: Local Provisioner has no Unicast Address assigned.")
            throw InvalidSource
        }
        return send(message = message, destination = destination.address, initialTtl = 1u)
    }

    /**
     * Sends the Proxy Configuration Message to the connected Proxy Node.
     *
     * This method will only work if the bearer uses is GATT Proxy. The message will be encrypted
     * and sent to the [Transmitter], which should deliver the PDU to the connected Node.
     *
     * @param message     Proxy Configuration message to be sent.
     * @throws IllegalStateException This method throws when the mesh network has not been created.
     */
    @Throws(IllegalStateException::class)
    suspend fun send(message: ProxyConfigurationMessage): ProxyConfigurationMessage? =
        networkManager?.send(message) ?: run {
            logger?.e(category = LogCategory.PROXY) { "Error: Mesh Network not created" }
            throw IllegalStateException("Network manager is not initialized")
        }

    /**
     * Observes network manager events.
     */
    private fun observeNetworkManagerEvents() {
        networkManager?.networkManagerEventFlow?.onEach {
            when (it) {
                NetworkManagerEvent.NetworkDidChange -> save()
                NetworkManagerEvent.NetworkDidReset -> {
                    network?.localProvisioner?.let { provisioner ->
                        val localElements = this@MeshNetworkManager.localElements
                        provisioner.network = null
                        create()
                        this@MeshNetworkManager.localElements = localElements
                    }
                }
            }
        }?.launchIn(scope = scope)
    }
}