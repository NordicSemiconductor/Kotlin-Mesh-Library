package no.nordicsemi.android.nrfmesh.core.data

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import javax.inject.Inject

class DataStoreRepository @Inject constructor(
    private val meshNetworkManager: MeshNetworkManager
) {
    val network: Flow<MeshNetwork> = meshNetworkManager.network

    suspend fun loadNetwork(): Boolean {
        if (!meshNetworkManager.load()) {
            meshNetworkManager.createMeshNetwork("Home Network")
        }
        return true
    }

    suspend fun importMeshNetwork(data: ByteArray) {
        meshNetworkManager.importMeshNetwork(data)
    }
}
