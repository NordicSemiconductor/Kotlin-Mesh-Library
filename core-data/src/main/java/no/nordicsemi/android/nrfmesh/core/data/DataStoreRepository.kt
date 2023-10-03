package no.nordicsemi.android.nrfmesh.core.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import no.nordicsemi.android.nrfmesh.core.common.dispatchers.Dispatcher
import no.nordicsemi.android.nrfmesh.core.common.dispatchers.MeshDispatchers
import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.UnicastRange
import no.nordicsemi.kotlin.mesh.core.model.serialization.config.NetworkConfiguration
import javax.inject.Inject

class DataStoreRepository @Inject constructor(
    private val meshNetworkManager: MeshNetworkManager,
    @Dispatcher(MeshDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) {

    val network = meshNetworkManager.meshNetwork
    /*val network: Flow<MeshNetwork> = flow {
        meshNetworkManager.meshNetwork?.let { emit(it) }
    }*/

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
}
