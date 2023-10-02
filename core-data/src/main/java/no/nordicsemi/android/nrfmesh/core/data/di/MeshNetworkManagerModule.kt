package no.nordicsemi.android.nrfmesh.core.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.android.nrfmesh.core.storage.MeshNetworkPropertiesStorage
import no.nordicsemi.android.nrfmesh.core.storage.MeshNetworkStorage
import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MeshNetworkManagerModule {

    @Provides
    @Singleton
    fun provideMeshManager(
        meshNetworkStorage: MeshNetworkStorage,
        meshNetworkPropertiesStorage: MeshNetworkPropertiesStorage,
        scope : CoroutineScope
    ) = MeshNetworkManager(
        storage = meshNetworkStorage,
        networkProperties = meshNetworkPropertiesStorage,
        scope = scope
    )
}