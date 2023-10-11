package no.nordicsemi.android.nrfmesh.core.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import no.nordicsemi.android.nrfmesh.core.common.dispatchers.Dispatcher
import no.nordicsemi.android.nrfmesh.core.common.dispatchers.MeshDispatchers
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DataStoreRepositoryModule {

    @Singleton
    @Provides
    fun provideDataStoreRepository(
        meshNetworkManager: MeshNetworkManager,
        @Dispatcher(MeshDispatchers.IO) ioDispatcher: CoroutineDispatcher
    ) = CoreDataRepository(
        meshNetworkManager = meshNetworkManager,
        ioDispatcher = ioDispatcher
    )
}