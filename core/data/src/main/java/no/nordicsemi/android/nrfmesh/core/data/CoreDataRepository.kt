package no.nordicsemi.android.nrfmesh.core.data

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.nordicsemi.android.common.permissions.ble.bluetooth.BluetoothStateManager
import no.nordicsemi.android.common.permissions.ble.location.LocationStateManager
import no.nordicsemi.android.common.permissions.ble.util.BlePermissionState
import no.nordicsemi.android.kotlin.mesh.bearer.android.utils.MeshProxyService
import no.nordicsemi.android.kotlin.mesh.bearer.pbgatt.PbGattBearer
import no.nordicsemi.android.nrfmesh.core.common.Utils.toAndroidLogLevel
import no.nordicsemi.android.nrfmesh.core.common.di.DefaultDispatcher
import no.nordicsemi.android.nrfmesh.core.common.di.IoDispatcher
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.mesh.bearer.Bearer
import no.nordicsemi.kotlin.mesh.bearer.BearerEvent
import no.nordicsemi.kotlin.mesh.bearer.gatt.GattBearer
import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager
import no.nordicsemi.kotlin.mesh.core.ProxyFilter
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.proxy.ProxyConfigurationMessage
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.Element
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
    @ApplicationContext private val context: Context,
    bluetoothStateManager: BluetoothStateManager,
    locationStateManager: LocationStateManager,
    private val preferences: DataStore<Preferences>,
    private val meshNetworkManager: MeshNetworkManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val centralManager: CentralManager,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : Logger {
    private var _proxyConnectionStateFlow = MutableStateFlow(ProxyConnectionState())
    val proxyConnectionStateFlow = _proxyConnectionStateFlow.asStateFlow()

    val network: SharedFlow<MeshNetwork>
        get() = meshNetworkManager.meshNetwork
    private lateinit var meshNetwork: MeshNetwork
    private var isBluetoothEnabled = false
    private var isLocationEnabled = false
    private var bearer: Bearer? = null
    private var connectionRequested = false

    val proxyFilter: ProxyFilter
        get() = meshNetworkManager.proxyFilter

    init {
        println("CoreDataRepository init: $centralManager")
        meshNetworkManager.logger = this
        preferences.data.onEach {
            _proxyConnectionStateFlow.value = _proxyConnectionStateFlow.value.copy(
                autoConnect = it[PreferenceKeys.PROXY_AUTO_CONNECT] == true
            )
        }.launchIn(CoroutineScope(defaultDispatcher))

        bluetoothStateManager.bluetoothState().onEach {
            isBluetoothEnabled = it is BlePermissionState.Available
        }.launchIn(CoroutineScope(defaultDispatcher))
        locationStateManager.locationState().onEach {
            isLocationEnabled = it is BlePermissionState.Available
        }.launchIn(CoroutineScope(defaultDispatcher))

        // Start automatic connectivity when the network changes

        // TODO need to check this on
        network.onEach { meshNetwork = it }
            .launchIn(scope = CoroutineScope(defaultDispatcher))
    }

    /**
     * Loads an existing mesh network or creates a new one.
     */
    suspend fun load() = withContext(ioDispatcher) {
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
        // TODO Implement scene model event handler related stuff
        val element0 = Element(
            location = Location.FIRST, _models = mutableListOf(
                Model(modelId = SigModelId(Model.SCENE_SERVER_MODEL_ID))
            )
        ).apply { name = "Primary Element" }
        meshNetworkManager.localElements = listOf(element0)
    }

    /**
     * Imports a mesh network.
     */
    suspend fun importMeshNetwork(data: ByteArray) {
        meshNetworkManager.import(data)
    }

    /**
     * Exports a mesh network.
     */
    suspend fun exportNetwork(configuration: NetworkConfiguration) =
        meshNetworkManager.export(configuration = configuration)

    suspend fun resetNetwork() = createNewMeshNetwork().also {
        onMeshNetworkChanged()
        save()
    }

    /**
     * Adds a network key to the network.
     */
    fun addNetworkKey(): NetworkKey = meshNetwork
        .add(name = "nRF Network Key ${meshNetwork.networkKeys.size}")
        .also { save() }

    /**
     * Adds an application key to the network.
     */
    fun addApplicationKey(
        name: String = "nRF Application Key ${meshNetwork.applicationKeys.size}",
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
        CoroutineScope(context = ioDispatcher)
            .launch { meshNetworkManager.save() }
    }

    /**
     * Creates a new mesh network.
     * @return MeshNetwork
     */
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
     * Connects to the unprovisioned node over PB-Gatt bearer.
     *
     * @param context Android context
     * @param device  Server device
     * @return [PbGattBearer] instance
     */
    suspend fun connectOverPbGattBearer(context: Context, device: Peripheral) =
        withContext(defaultDispatcher) {
            if (bearer is GattBearer) bearer?.close()
            PbGattBearer(context = context, centralManager = centralManager, peripheral = device)
                .also {
                    it.open()
                    bearer = it
                }
        }

    /**
     * Connects to the provisioned node over Gatt bearer.
     *
     * @param context          Android context
     * @param peripheral       Server device
     * @return [PbGattBearer]  instance
     */
    suspend fun connectOverGattBearer(context: Context, peripheral: Peripheral) =
        withContext(defaultDispatcher) {
            if ((bearer as? PbGattBearer)?.isOpen == true) bearer?.close()
            _proxyConnectionStateFlow.value = _proxyConnectionStateFlow.value.copy(
                connectionState = NetworkConnectionState.Connecting(peripheral = peripheral)
            )
            GattBearer(context = context, centralManager = centralManager, peripheral = peripheral)
                .also {
                    meshNetworkManager.setMeshBearerType(meshBearer = it)
                    bearer = it
                    it.open()
                    if (it.isOpen) {
                        _proxyConnectionStateFlow.value = _proxyConnectionStateFlow
                            .value.copy(connectionState = NetworkConnectionState.Connected(
                                peripheral = peripheral
                            )
                        )
                    }
                }
        }

    /**
     * Disconnects from the proxy node.
     */
    suspend fun disconnect() = withContext(defaultDispatcher) {
        bearer?.let { bearer ->
            if (bearer.isOpen) {
                bearer.close()
                bearer.state.first { it is BearerEvent.Closed }
                _proxyConnectionStateFlow.value = _proxyConnectionStateFlow.value.copy(
                    connectionState = NetworkConnectionState.Disconnected
                )
            }
        }
    }

    /**
     * Marks as bluetooth permissions are granted.
     */
    fun onBluetoothEnabled(enabled: Boolean) {
        isBluetoothEnabled = enabled
    }

    /**
     * Marks as location permissions are granted.
     */
    fun onLocationEnabled(enabled: Boolean) {
        isLocationEnabled = enabled
    }

    /**
     * Starts automatic connectivity to the proxy node.
     *
     * @param meshNetwork Mesh network required to match the proxy node.
     */
    suspend fun startAutomaticConnectivity(meshNetwork: MeshNetwork?) {
        if (isBluetoothEnabled && isLocationEnabled) {
            connectToProxy(meshNetwork)
        }
    }

    /**
     * Scans and connects to the proxy node if found.
     *
     * @param meshNetwork Mesh network required to match the proxy node.
     */
    private tailrec suspend fun connectToProxy(meshNetwork: MeshNetwork?) {
        val autoConnectProxy = _proxyConnectionStateFlow.value.autoConnect
        if (!autoConnectProxy) return
        if (connectionRequested) return
        connectionRequested = true
        require(bearer == null || !bearer!!.isOpen) { return }
        val peripheral = scanForProxy(meshNetwork)
        val bearer = connectOverGattBearer(context = context, peripheral = peripheral)
        bearer.state.filter { it is BearerEvent.Closed }.first()
        connectionRequested = false
        // Retry connecting
        connectToProxy(meshNetwork)
    }

    /**
     * Scans for the proxy node.
     *
     * @param meshNetwork Mesh network required to match the proxy node.
     * @return the peripheral containing the proxy node.
     */
    @OptIn(ExperimentalUuidApi::class)
    @SuppressLint("MissingPermission")
    suspend fun scanForProxy(meshNetwork: MeshNetwork?): Peripheral {
        _proxyConnectionStateFlow.value = _proxyConnectionStateFlow.value.copy(
            connectionState = NetworkConnectionState.Scanning
        )
        return centralManager
            .scan { ServiceUuid(uuid = MeshProxyService.uuid) }
            .first {
                val serviceData = it.advertisingData.serviceData[MeshProxyService.uuid]
                serviceData?.takeIf {
                    serviceData.isNotEmpty()
                }?.let { data ->
                    meshNetwork?.let { network ->
                        data.run {
                            when {
                                nodeIdentity() != null -> network.matches(nodeIdentity()!!)
                                networkIdentity() != null -> network.matches(networkIdentity()!!)
                                else -> false
                            }
                        }
                    } == false
                } == false
            }.peripheral
    }

    /**
     * Enables or disables automatic connectivity to a proxy node.
     *
     * @param meshNetwork Mesh network required to match a proxy node.
     * @param enabled     True to enable, false to disable.
     */
    suspend fun enableAutoConnectProxy(meshNetwork: MeshNetwork?, enabled: Boolean) {
        _proxyConnectionStateFlow.value =
            _proxyConnectionStateFlow.value.copy(autoConnect = enabled)
        preferences.edit { preferences ->
            preferences[PreferenceKeys.PROXY_AUTO_CONNECT] = enabled
        }
        if (enabled) startAutomaticConnectivity(meshNetwork)
    }

    /**
     * Sends a proxy configuration message to the proxy node.
     */
    suspend fun send(message: ProxyConfigurationMessage) = withContext(defaultDispatcher) {
        meshNetworkManager.send(message)
    }

    /**
     * Sends an acknowledged config messages to the given node.
     *
     * @param node    Destination node.
     * @param message Message to be sent.
     */
    suspend fun send(node: Node, message: AcknowledgedConfigMessage) = withContext(
        context = defaultDispatcher
    ) {
        if (bearer != null && bearer!!.isOpen) {
            meshNetworkManager.send(message = message, node = node, initialTtl = null)
                .also {
                    log(
                        message = it?.toString() ?: "",
                        category = LogCategory.ACCESS,
                        level = LogLevel.INFO
                    )
                }
        } else {
            throw IllegalStateException("Bearer is not open")
        }
    }

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