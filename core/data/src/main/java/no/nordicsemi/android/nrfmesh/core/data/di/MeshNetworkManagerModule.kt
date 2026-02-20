package no.nordicsemi.android.nrfmesh.core.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineDispatcher
import no.nordicsemi.android.nrfmesh.core.common.di.IoDispatcher
import no.nordicsemi.android.nrfmesh.core.data.storage.MeshNetworkStorage
import no.nordicsemi.android.nrfmesh.core.data.storage.MeshSecurePropertiesStorage
import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager

@Module
@InstallIn(ActivityRetainedComponent::class)
object MeshNetworkManagerModule {

    @Provides
    @ActivityRetainedScoped
    fun provideMeshManager(
        meshNetworkStorage: MeshNetworkStorage,
        meshNetworkPropertiesStorage: MeshSecurePropertiesStorage,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ) = MeshNetworkManager(
        storage = meshNetworkStorage,
        secureProperties = meshNetworkPropertiesStorage,
        ioDispatcher = ioDispatcher
    )
}