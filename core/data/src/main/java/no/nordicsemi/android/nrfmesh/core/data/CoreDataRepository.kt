package no.nordicsemi.android.nrfmesh.core.data

import android.os.Build
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.nordicsemi.android.nrfmesh.core.common.Utils.toAndroidLogLevel
import no.nordicsemi.android.nrfmesh.core.common.di.IoDispatcher
import no.nordicsemi.android.nrfmesh.core.data.VendorModelIds.LE_PAIRING_INITIATOR
import no.nordicsemi.android.nrfmesh.core.data.bearer.AndroidGattBearer
import no.nordicsemi.android.nrfmesh.core.data.bearer.AndroidPbGattBearer
import no.nordicsemi.android.nrfmesh.core.data.meshnetwork.simpleonoff.SimpleOnOffClientHandler
import no.nordicsemi.android.nrfmesh.core.data.modeleventhandlers.GenericDefaultTransitionTimeServer
import no.nordicsemi.android.nrfmesh.core.data.modeleventhandlers.GenericOnOffClientEventHandler
import no.nordicsemi.android.nrfmesh.core.data.modeleventhandlers.GenericOnOffServer
import no.nordicsemi.android.nrfmesh.core.data.storage.SceneStatesDataStoreStorage
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.mesh.bearer.Bearer
import no.nordicsemi.kotlin.mesh.bearer.BearerEvent
import no.nordicsemi.kotlin.mesh.bearer.gatt.utils.MeshProxyService
import no.nordicsemi.kotlin.mesh.bearer.provisioning.ProvisioningBearer
import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager
import no.nordicsemi.kotlin.mesh.core.ProxyFilter
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.BaseMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.UnacknowledgedMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.proxy.ProxyConfigurationMessage
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.Element
import no.nordicsemi.kotlin.mesh.core.model.Group
import no.nordicsemi.kotlin.mesh.core.model.GroupAddress
import no.nordicsemi.kotlin.mesh.core.model.GroupRange
import no.nordicsemi.kotlin.mesh.core.model.Location
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.Node
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import no.nordicsemi.kotlin.mesh.core.model.SceneRange
import no.nordicsemi.kotlin.mesh.core.model.SigModelId
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.UnicastRange
import no.nordicsemi.kotlin.mesh.core.model.VendorModelId
import no.nordicsemi.kotlin.mesh.core.model.serialization.config.NetworkConfiguration
import no.nordicsemi.kotlin.mesh.core.util.networkIdentity
import no.nordicsemi.kotlin.mesh.core.util.nodeIdentity
import no.nordicsemi.kotlin.mesh.logger.LogCategory
import no.nordicsemi.kotlin.mesh.logger.LogLevel
import no.nordicsemi.kotlin.mesh.logger.Logger
import java.util.Locale
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi

private object PreferenceKeys {
    val PROXY_AUTO_CONNECT = booleanPreferencesKey("proxy_auto_connect")
}

class CoreDataRepository @Inject constructor(
    private val preferences: DataStore<Preferences>,
    private val meshNetworkManager: MeshNetworkManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val storage: SceneStatesDataStoreStorage,
    private val centralManager: CentralManager,
) : Logger {
    private var _proxyConnectionStateFlow = MutableStateFlow(ProxyConnectionState())
    val proxyConnectionStateFlow = _proxyConnectionStateFlow.asStateFlow()

    val network: SharedFlow<MeshNetwork>
        get() = meshNetworkManager.meshNetwork

    val incomingMessages: SharedFlow<BaseMeshMessage>
        get() = meshNetworkManager.incomingMeshMessages

    private lateinit var meshNetwork: MeshNetwork
    private var bearer: Bearer? = null
    private var connectionRequested = false

    val proxyFilter: ProxyFilter
        get() = meshNetworkManager.proxyFilter
    val ivUpdateTestMode: Boolean
        get() = meshNetworkManager.networkParameters.ivUpdateTestMode

    private val ioScope = CoroutineScope(context = SupervisorJob() + ioDispatcher)


    init {
        // Initialize the mesh network manager logger
        meshNetworkManager.logger = this
        // Observe changes to the mesh network
        observeNetworkChanges()
        // Observe proxy connection state changes
        observerAutomaticProxyConnectionState()

        initNetwork()
    }

    private fun observeNetworkChanges() {
        network.onEach {
            meshNetwork = it
            // Start automatic connectivity
            // startAutomaticConnectivity(meshNetwork = it)
        }.launchIn(scope = ioScope)

    }

    private fun observerAutomaticProxyConnectionState() {
        preferences.data.onEach {
            _proxyConnectionStateFlow.value = _proxyConnectionStateFlow.value.copy(
                autoConnect = it[PreferenceKeys.PROXY_AUTO_CONNECT] == true
            )
        }.launchIn(scope = ioScope)
    }

    private fun initNetwork() {
        ioScope.launch {
            val network = load()
            // Connect to the proxy node if automatic connectivity is enabled
            startAutomaticConnectivity(meshNetwork = meshNetwork)
        }
    }

    /**
     * Loads an existing mesh network or creates a new one.
     */
    suspend fun load() = withContext(context = ioDispatcher) {
        val meshNetwork = if (!meshNetworkManager.load()) {
            createNewMeshNetwork()
        } else {
            meshNetworkManager.meshNetwork.first()
        }
        onMeshNetworkChanged()
        meshNetwork
    }

    /**
     * Invoked when the mesh network has changed. This will setup the local elements and
     * reinitialise the connection to the proxy node. This will ensure that the user is connected to
     * the correct network.
     */
    private fun onMeshNetworkChanged() {
        val defaultTransitionTimeServer = GenericDefaultTransitionTimeServer()
        // Sets up the local Elements on the phone
        val element0 = Element(
            _name = "Primary Element",
            location = Location.FIRST,
            _models = mutableListOf(
                Model(modelId = SigModelId(modelIdentifier = Model.SCENE_SERVER_MODEL_ID)),
                Model(modelId = SigModelId(modelIdentifier = Model.SCENE_SETUP_SERVER_MODEL_ID)),
                Model(modelId = SigModelId(modelIdentifier = Model.SENSOR_CLIENT_MODEL_ID)),
                Model(modelId = SigModelId(modelIdentifier = Model.GENERIC_POWER_ON_OFF_CLIENT_MODEL_ID)),
                Model(modelId = SigModelId(modelIdentifier = Model.GENERIC_DEFAULT_TRANSITION_TIME_SERVER_MODEL_ID)),
                Model(modelId = SigModelId(modelIdentifier = Model.GENERIC_DEFAULT_TRANSITION_TIME_CLIENT_MODEL_ID)),
                // Generic OnOff and Generic Level models defined by SIG
                Model(
                    modelId = SigModelId(modelIdentifier = Model.GENERIC_ON_OFF_SERVER_MODEL_ID),
                    handler = GenericOnOffServer(
                        ioDispatcher = ioDispatcher,
                        storage = storage,
                        defaultTransitionTimeServer = defaultTransitionTimeServer
                    )
                ),
                Model(modelId = SigModelId(modelIdentifier = Model.GENERIC_LEVEL_SERVER_MODEL_ID)),
                Model(
                    modelId = SigModelId(modelIdentifier = Model.GENERIC_ON_OFF_CLIENT_MODEL_ID),
                    handler = GenericOnOffClientEventHandler()
                ),
                Model(modelId = SigModelId(modelIdentifier = Model.GENERIC_LEVEL_CLIENT_MODEL_ID)),
                Model(modelId = SigModelId(modelIdentifier = Model.LIGHT_LC_CLIENT_MODEL_ID)),
                // Nordic Pairing Initiator model
                Model(
                    modelId = VendorModelId(
                        modelIdentifier = LE_PAIRING_INITIATOR,
                        companyIdentifier = NORDIC_SEMICONDUCTOR_COMPANY_ID
                    )
                ),
                // A simple vendor model
                Model(
                    modelId = VendorModelId(
                        modelIdentifier = VendorModelIds.SIMPLE_ON_OFF_CLIENT_MODEL_ID,
                        companyIdentifier = NORDIC_SEMICONDUCTOR_COMPANY_ID
                    ),
                    handler = SimpleOnOffClientHandler(this)
                )
            )
        )
        val element1 = Element(
            _name = "Secondary Element",
            location = Location.SECOND,
            _models = mutableListOf(
                Model(modelId = SigModelId(Model.GENERIC_ON_OFF_SERVER_MODEL_ID)),
                Model(modelId = SigModelId(Model.GENERIC_LEVEL_SERVER_MODEL_ID)),
                Model(modelId = SigModelId(Model.GENERIC_ON_OFF_CLIENT_MODEL_ID)),
                Model(modelId = SigModelId(Model.GENERIC_LEVEL_CLIENT_MODEL_ID))
            )
        )
        meshNetworkManager.localElements = listOf(element0, element1)
    }

    /**
     * Imports a mesh network.
     */
    suspend fun importMeshNetwork(data: ByteArray) = meshNetworkManager.import(data)

    /**
     * Exports a mesh network.
     */
    fun exportNetwork(configuration: NetworkConfiguration) =
        meshNetworkManager.export(configuration = configuration)

    suspend fun resetNetwork() = createNewMeshNetwork().also {
        onMeshNetworkChanged()
        save()
    }

    /**
     * Adds a network key to the network.
     */
    fun addNetworkKey(): NetworkKey = meshNetwork
        .add(name = "Network Key ${meshNetwork.networkKeys.size}")
        .also { save() }

    /**
     * Adds an application key to the network.
     *
     * @param name            Name of the Application Key.
     * @param boundNetworkKey Bound Network Key
     */
    fun addApplicationKey(
        name: String = "Application Key ${meshNetwork.applicationKeys.size + 1}",
        boundNetworkKey: NetworkKey,
    ): ApplicationKey = meshNetwork.add(
        name = name,
        boundNetworkKey = boundNetworkKey
    ).also {
        save()
    }

    /**
     * Saves the mesh network.
     */
    fun save() {
        ioScope.launch { meshNetworkManager.save() }
    }

    /**
     * Creates a new mesh network.
     * @return MeshNetwork
     */
    @OptIn(ExperimentalUuidApi::class)
    private suspend fun createNewMeshNetwork() = meshNetworkManager.create(
        provisioner = Provisioner(name = createProvisionerName()).apply {
            allocate(
                range = UnicastRange(
                    lowAddress = UnicastAddress(address = 0x0001),
                    highAddress = UnicastAddress(address = 0x199B)
                )
            )
            allocate(
                range = GroupRange(
                    lowAddress = GroupAddress(address = 0xC000),
                    highAddress = GroupAddress(address = 0xCC9A)
                )
            )
            allocate(range = SceneRange(firstScene = 0x0001u, lastScene = 0x3333u))
        }
    ).also { meshNetworkManager.save() }

    /**
     * Enables or disables automatic connectivity to a proxy node.
     *
     * @param meshNetwork Mesh network required to match a proxy node.
     * @param enabled     True to enable, false to disable.
     */
    suspend fun toggleAutomaticConnection(enabled: Boolean): Unit =
        withContext(context = ioDispatcher) {
            _proxyConnectionStateFlow.value =
                _proxyConnectionStateFlow.value.copy(autoConnect = enabled)
            preferences.edit { preferences ->
                preferences[PreferenceKeys.PROXY_AUTO_CONNECT] = enabled
            }
        }

    /**
     * Starts automatic connectivity to the proxy node.
     *
     * @param meshNetwork Mesh network required to match the proxy node.
     */
    suspend fun startAutomaticConnectivity(meshNetwork: MeshNetwork?) {
        val autoConnectProxy = _proxyConnectionStateFlow.value.autoConnect
        if (!autoConnectProxy) return
        withContext(context = ioDispatcher) {
            connectToProxy(meshNetwork)
        }
    }

    /**
     * Scans and connects to the proxy node if found.
     *
     * @param meshNetwork Mesh network required to match the proxy node.
     */
    private suspend fun connectToProxy(meshNetwork: MeshNetwork?) {
        require(bearer == null || !bearer!!.isOpen) { return }
        if (connectionRequested) return
        connectionRequested = true
        val peripheral = scanForProxy(meshNetwork)
        // If the peripheral is null, it means that the proxy node was not found or scanning failed.
        // If so we can safely return here and retry later.
        if (peripheral == null) {
            log(
                message = "Proxy node not found",
                category = LogCategory.BEARER,
                level = LogLevel.INFO
            )
            _proxyConnectionStateFlow.value = _proxyConnectionStateFlow.value.copy(
                connectionState = NetworkConnectionState.Disconnected
            )
            connectionRequested = false
            return
        }
        try {
            // Connect to the proxy node over GATT bearer. This method is blocking and will only
            // return once the connection is established or failed.
            connectOverGattBearer(peripheral = peripheral)
        } catch (e: Exception) {
            log(
                message = "Failed to connect to proxy node: ${e.message}",
                category = LogCategory.BEARER,
                level = LogLevel.ERROR
            )
            _proxyConnectionStateFlow.value = _proxyConnectionStateFlow.value.copy(
                connectionState = NetworkConnectionState.Disconnected
            )
        } finally {
            connectionRequested = false
        }
        // Let's observe the connectivity in the connectOverGattBearer to restart connecting
        // // Retry connecting
        // connectToProxy(meshNetwork)
    }

    /**
     * Scans for the proxy node.
     *
     * @param meshNetwork Mesh network required to match the proxy node.
     * @return the peripheral containing the proxy node.
     */
    @OptIn(ExperimentalUuidApi::class)
    suspend fun scanForProxy(meshNetwork: MeshNetwork?) = try {
        _proxyConnectionStateFlow.value = _proxyConnectionStateFlow.value.copy(
            connectionState = NetworkConnectionState.Scanning
        )
        centralManager
            .scan { ServiceUuid(uuid = MeshProxyService.uuid) }
            .first {
                val serviceData = it.advertisingData.serviceData[MeshProxyService.uuid]
                serviceData
                    ?.takeIf { serviceData.isNotEmpty() }
                    ?.let { data ->
                        meshNetwork
                            ?.let { network ->
                                data
                                    .run {
                                        when {
                                            nodeIdentity() != null -> network.matches(
                                                nodeIdentity = nodeIdentity()!!
                                            )

                                            networkIdentity() != null -> network.matches(
                                                networkId = networkIdentity()!!
                                            )

                                            else -> false
                                        }
                                    }
                            } == false
                    } == false
            }.peripheral
    } catch (e: Exception) {
        log(
            message = "Failed to scan for proxy node: ${e.message}",
            category = LogCategory.BEARER,
            level = LogLevel.ERROR
        )
        null
    }

    /**
     * Connects to the unprovisioned node over PB-Gatt bearer.
     *
     * @param device  Server device
     * @return [ProvisioningBearer] instance
     */
    suspend fun connectOverPbGattBearer(device: Peripheral) =
        withContext(context = ioDispatcher) {
            if (bearer is AndroidGattBearer) bearer?.close()
            AndroidPbGattBearer(
                centralManager = centralManager,
                peripheral = device
            ).also {
                it.logger = this@CoreDataRepository
                it.open()
                bearer = it
            }
        }

    /**
     * Connects to the provisioned node over Gatt bearer.
     *
     * @param peripheral       Server device
     * @return [ProvisioningBearer]  instance
     */
    suspend fun connectOverGattBearer(peripheral: Peripheral) =
        withContext(context = ioDispatcher) {
            if (bearer is AndroidPbGattBearer) bearer?.close()
            _proxyConnectionStateFlow.value = _proxyConnectionStateFlow.value.copy(
                connectionState = NetworkConnectionState.Connecting(peripheral = peripheral)
            )
            AndroidGattBearer(
                centralManager = centralManager,
                peripheral = peripheral,
                ioDispatcher = ioDispatcher
            ).also { it ->
                it.logger = this@CoreDataRepository
                meshNetworkManager.meshBearer = it
                bearer = it
                it.open()
                _proxyConnectionStateFlow.value = _proxyConnectionStateFlow
                    .value.copy(
                        connectionState = NetworkConnectionState.Connected(
                            peripheral = peripheral
                        )
                    )

                // Wait for the bearer to disconnect
                ioScope.launch {
                    it.state.first { it is BearerEvent.Closed }
                    bearer = null
                    meshNetworkManager.proxyFilter.proxyDidDisconnect()
                    // We add a slight delay here before connecting again if the connection drops
                    // Note: connection will only be established if automatic connectivity is
                    // enabled as per the implementation.
                    delay(timeMillis = 1500)
                    startAutomaticConnectivity(meshNetwork)
                }
            }
        }

    /**
     * Disconnects from the proxy node.
     */
    suspend fun disconnect() = withContext(context = ioDispatcher) {
        bearer?.let { bearer ->
            if (bearer.isOpen) {
                bearer.close()
                // bearer.state.first { it is BearerEvent.Closed }
                _proxyConnectionStateFlow.value = _proxyConnectionStateFlow.value.copy(
                    connectionState = NetworkConnectionState.Disconnected
                )
            }
        }.also {
            // Bearer null
            bearer = null
        }
    }

    /**
     * Sends a proxy configuration message to the proxy node.
     *
     * @param message Proxy configuration message to be sent.
     */
    suspend fun send(message: ProxyConfigurationMessage) = withContext(context = ioDispatcher) {
        meshNetworkManager.send(message)
    }

    /**
     * Sends an acknowledged config messages to the given node.
     *
     * @param node    Destination node.
     * @param message Message to be sent.
     */
    suspend fun send(node: Node, message: AcknowledgedConfigMessage) =
        withContext(context = ioDispatcher) {
            if (node.primaryUnicastAddress == meshNetwork.localProvisioner?.node?.primaryUnicastAddress)
                meshNetworkManager.sendToLocalNode(message = message)
            else
                meshNetworkManager.send(message = message, node = node, initialTtl = null)
        }

    /**
     * Sends an unacknowledged mesh message to the given model.
     *
     * @param model          Destination model.
     * @param unackedMessage Unacknowledged mesh message to be sent.
     */
    suspend fun send(model: Model, unackedMessage: UnacknowledgedMeshMessage) = withContext(
        context = ioDispatcher
    ) {
        meshNetworkManager.send(
            model = model,
            message = unackedMessage,
            initialTtl = if (isDestinedToLocalNode(model.parentElement?.unicastAddress)) {
                1.toUByte() // Use TTL 1 for messages destined to the local node
            } else {
                null
            }
        )
    }

    /**
     * Sends an unacknowledged mesh message to the given model.
     *
     * @param group          Group to which the message should be sent.
     * @param unackedMessage Unacknowledged mesh message to be sent.
     * @param key            Application key to be used for sending the message.
     */
    suspend fun send(group: Group, unackedMessage: UnacknowledgedMeshMessage, key: ApplicationKey) =
        withContext(context = ioDispatcher) {
            meshNetworkManager.send(group = group, message = unackedMessage, key = key)
        }

    /**
     * Sends an acknowledged mesh message to the given model.
     *
     * @param model        Destination model.
     * @param ackedMessage Unacknowledged mesh message to be sent.
     */
    suspend fun send(model: Model, ackedMessage: AcknowledgedMeshMessage) = withContext(
        context = ioDispatcher
    ) {
        meshNetworkManager.send(
            model = model,
            message = ackedMessage,
            initialTtl = if (isDestinedToLocalNode(model.parentElement?.unicastAddress)) {
                1.toUByte() // Use TTL 1 for messages destined to the local node
            } else {
                null
            }
        )
    }

    private fun isDestinedToLocalNode(destination: UnicastAddress?) =
        destination == meshNetwork.localProvisioner?.node?.primaryUnicastAddress

    override fun log(message: String, category: LogCategory, level: LogLevel) {
        Log.println(level.toAndroidLogLevel(), category.category, message)
    }

    /**
     * Creates provisioner name based on the provisioner device model.
     */
    fun createProvisionerName(): String = Build.MODEL.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.ROOT)
        else it.toString()
    }

    fun toggleIvUpdateTestMode(flag: Boolean) {
        meshNetworkManager.networkParameters.ivUpdateTestMode = flag
    }
}

sealed class NetworkConnectionState {
    data object Scanning : NetworkConnectionState()
    data class Connecting(val peripheral: Peripheral) : NetworkConnectionState()
    data class Connected(val peripheral: Peripheral) : NetworkConnectionState()
    data object Disconnected : NetworkConnectionState()
}

data class ProxyConnectionState(
    val autoConnect: Boolean = false,
    val connectionState: NetworkConnectionState = NetworkConnectionState.Disconnected,
)