package no.nordicsemi.android.nrfmesh.core.data

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.mesh.bearer.pbgatt.PbGattBearer
import no.nordicsemi.android.nrfmesh.core.common.dispatchers.Dispatcher
import no.nordicsemi.android.nrfmesh.core.common.dispatchers.MeshDispatchers
import no.nordicsemi.kotlin.mesh.bearer.Bearer
import no.nordicsemi.kotlin.mesh.bearer.gatt.GattBearer
import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.UnicastRange
import no.nordicsemi.kotlin.mesh.core.model.serialization.config.NetworkConfiguration
import javax.inject.Inject

class CoreDataRepository @Inject constructor(
    private val meshNetworkManager: MeshNetworkManager,
    @Dispatcher(MeshDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) {

    val network = meshNetworkManager.meshNetwork

    /*val network: Flow<MeshNetwork> = flow {
        meshNetworkManager.meshNetwork?.let { emit(it) }
    }*/
    private var bearer: Bearer? = null

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
            if (bearer is GattBearer) {
                bearer?.close()
            }
            PbGattBearer(
                context = context,
                device = device
            ).also {
                it.open()
                bearer = it
            }
        }

    suspend fun connectOverGattBearer(context: Context, device: ServerDevice) =
        withContext(ioDispatcher) {
            // TODO
        }

    suspend fun close() = withContext(ioDispatcher) {
        bearer?.close()
    }
}
