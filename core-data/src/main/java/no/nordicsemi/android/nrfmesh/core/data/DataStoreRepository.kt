package no.nordicsemi.android.nrfmesh.core.data

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.serialization.config.NetworkConfiguration
import javax.inject.Inject

class DataStoreRepository @Inject constructor(
    private val meshNetworkManager: MeshNetworkManager
) {

    val network: Flow<MeshNetwork> = meshNetworkManager.network

    suspend fun importMeshNetwork(data: ByteArray) {
        meshNetworkManager.import(data)
    }

    suspend fun exportNetwork(configuration: NetworkConfiguration) =
        meshNetworkManager.export(configuration = configuration)

    suspend fun save() = meshNetworkManager.save()

}
