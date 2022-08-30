package no.nordicsemi.android.nrfmesh.core.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import no.nordicsemi.android.nrfmesh.core.common.dispatchers.Dispatcher
import no.nordicsemi.android.nrfmesh.core.common.dispatchers.MeshDispatchers
import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.serialization.config.NetworkConfiguration
import javax.inject.Inject

class DataStoreRepository @Inject constructor(
    private val meshNetworkManager: MeshNetworkManager,
    @Dispatcher(MeshDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) {
    val network: Flow<MeshNetwork> = meshNetworkManager.network

    suspend fun load() = withContext(ioDispatcher) {
        meshNetworkManager.load()
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
