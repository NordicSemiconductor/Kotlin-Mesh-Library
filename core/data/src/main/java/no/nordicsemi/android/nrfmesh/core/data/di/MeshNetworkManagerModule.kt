package no.nordicsemi.android.nrfmesh.core.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import no.nordicsemi.android.nrfmesh.core.common.dispatchers.di.DefaultDispatcher
import no.nordicsemi.android.nrfmesh.core.data.storage.MeshNetworkStorage
import no.nordicsemi.android.nrfmesh.core.data.storage.MeshSecurePropertiesStorage
import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MeshNetworkManagerModule {

    @Provides
    @Singleton
    fun provideMeshManager(
        meshNetworkStorage: MeshNetworkStorage,
        meshNetworkPropertiesStorage: MeshSecurePropertiesStorage,
        @DefaultDispatcher defaultDispatcher: CoroutineDispatcher
    ) = MeshNetworkManager(
        storage = meshNetworkStorage,
        secureProperties = meshNetworkPropertiesStorage,
        scope = CoroutineScope(defaultDispatcher + Job())
    )
}