package no.nordicsemi.android.nrfmesh.core.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import no.nordicsemi.android.nrfmesh.core.data.storage.MeshNetworkPropertiesStorage
import no.nordicsemi.android.nrfmesh.core.data.storage.MeshNetworkStorage
import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MeshNetworkManagerModule {

    @Provides
    @Singleton
    fun provideMeshManager(
        meshNetworkStorage: MeshNetworkStorage,
        meshNetworkPropertiesStorage: MeshNetworkPropertiesStorage
    ) = MeshNetworkManager(
        storage = meshNetworkStorage,
        networkProperties = meshNetworkPropertiesStorage,
        scope = CoroutineScope(Dispatchers.Default + Job())
    )
}