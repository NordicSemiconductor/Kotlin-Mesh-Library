package no.nordicsemi.android.nrfmesh.core.data

import android.annotation.SuppressLint
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResult
import no.nordicsemi.android.kotlin.ble.scanner.BleScanner
import no.nordicsemi.android.kotlin.mesh.bearer.android.utils.MeshProxyService
import no.nordicsemi.android.kotlin.mesh.bearer.pbgatt.PbGattBearer
import no.nordicsemi.android.nrfmesh.core.common.Utils.toAndroidLogLevel
import no.nordicsemi.android.nrfmesh.core.common.dispatchers.Dispatcher
import no.nordicsemi.android.nrfmesh.core.common.dispatchers.MeshDispatchers
import no.nordicsemi.kotlin.mesh.bearer.Bearer
import no.nordicsemi.kotlin.mesh.bearer.BearerEvent
import no.nordicsemi.kotlin.mesh.bearer.gatt.GattBearer
import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Node
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.UnicastRange
import no.nordicsemi.kotlin.mesh.core.model.serialization.config.NetworkConfiguration
import no.nordicsemi.kotlin.mesh.core.util.networkIdentity
import no.nordicsemi.kotlin.mesh.core.util.nodeIdentity
import no.nordicsemi.kotlin.mesh.logger.LogCategory
import no.nordicsemi.kotlin.mesh.logger.LogLevel
import no.nordicsemi.kotlin.mesh.logger.Logger
import javax.inject.Inject

class CoreDataRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val meshNetworkManager: MeshNetworkManager,
    private val scanner: BleScanner,
    @Dispatcher(MeshDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : Logger {

    private val proxyScanner = CoroutineScope(Dispatchers.Main + SupervisorJob())
    val network = meshNetworkManager.meshNetwork

    private var bearer: Bearer? = null
    var autoConnectProxy = true
        private set

    init {
        meshNetworkManager.logger = this
    }

    suspend fun load() = withContext(ioDispatcher) {
        if (!meshNetworkManager.load()) {
            val provisioner = Provisioner(name = "Mesh Provisioner")
            provisioner.allocate(UnicastRange(UnicastAddress(1), UnicastAddress(0x7FFF)))
            meshNetworkManager.create(provisioner = provisioner)
        }
        true
    }

    suspend fun importMeshNetwork(data: ByteArray) {
        meshNetworkManager.import(data)
    }

    suspend fun exportNetwork(configuration: NetworkConfiguration) =
        meshNetworkManager.export(configuration = configuration)

    suspend fun save() = withContext(ioDispatcher) {
        meshNetworkManager.save()
    }

    suspend fun connectOverPbGattBearer(context: Context, device: ServerDevice) =
        withContext(ioDispatcher) {
            if (bearer is GattBearer) bearer?.close()
            PbGattBearer(context = context, device = device).also {
                it.open()
                bearer = it
            }
        }

    suspend fun connectOverGattBearer(context: Context, device: ServerDevice) =
        withContext(ioDispatcher) {
            if (bearer is PbGattBearer) bearer?.close()
            GattBearer(context = context, device = device).also {
                meshNetworkManager.meshBearer = it
                bearer = it
                it.open()
            }
        }

    suspend fun disconnect() = withContext(ioDispatcher) {
        bearer?.close()
    }

    /**
     * Starts automatic connectivity to the proxy node.
     *
     * @param meshNetwork Mesh network required to match the proxy node.
     */
    fun startAutomaticConnectivity(meshNetwork: MeshNetwork?) {
        proxyScanner.launch {
            connect(meshNetwork)
        }
    }

    /**
     * Scans and connects to the proxy node if found.
     *
     * @param meshNetwork Mesh network required to match the proxy node.
     */
    private tailrec suspend fun connect(meshNetwork: MeshNetwork?) {
        if (autoConnectProxy) {
            val device = scanForProxy(meshNetwork)
            val bearer = connectOverGattBearer(context = context, device = device)
            bearer.state.filter { it is BearerEvent.Closed }.first()
            connect(meshNetwork)
        }
    }

    /**
     * Scans for the proxy node.
     *
     * @param meshNetwork Mesh network required to match the proxy node.
     * @return [BleScanResult] containing the proxy node.
     */
    @SuppressLint("MissingPermission")
    suspend fun scanForProxy(meshNetwork: MeshNetwork?): ServerDevice = scanner.scan().first {
        val data = it.data?.scanRecord?.serviceData
            ?.get(ParcelUuid(MeshProxyService.uuid))
        meshNetwork?.takeIf {
            data != null
        }?.let { meshNetwork ->
            data!!.value.nodeIdentity()?.let { nodeIdentity ->
                meshNetwork.matches(nodeIdentity)
            } ?: false || data.value.networkIdentity()?.let { networkId ->
                meshNetwork.matches(networkId)
            } ?: false
        } ?: false
    }.device

    /**
     * Stops the proxy scanner.
     */
    fun stopProxyScanner() {
        proxyScanner.cancel("Scanner stopped")
    }

    /**
     * Enables or disables automatic connectivity to the proxy node.
     *
     * @param meshNetwork Mesh network required to match the proxy node.
     * @param enabled     True to enable, false to disable.
     */
    fun enableAutoConnectProxy(meshNetwork: MeshNetwork?, enabled: Boolean) {
        autoConnectProxy = enabled
        if (enabled) startAutomaticConnectivity(meshNetwork)
        else stopProxyScanner()
    }

    /**
     * Sends an acknowledged config messages to the given node.
     *
     * @param node    Destination node.
     * @param message Message to be sent.
     */
    suspend fun sendMessage(node: Node, message: AcknowledgedConfigMessage) = withContext(
        context = ioDispatcher
    ) {
        val response = meshNetworkManager.send(
            message = message, node = node, initialTtl = null
        )
        log(
            message = response?.toString() ?: "",
            category = LogCategory.ACCESS,
            level = LogLevel.INFO
        )
    }

    override fun log(message: String, category: LogCategory, level: LogLevel) {
        Log.println(level.toAndroidLogLevel(), category.category, message)
    }
}
