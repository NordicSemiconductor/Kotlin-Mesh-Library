package no.nordicsemi.android.nrfmesh.core.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DataStoreRepositoryModule {

    @Singleton
    @Provides
    fun provideDataStoreRepository(meshNetworkManager: MeshNetworkManager) = DataStoreRepository(
        meshNetworkManager = meshNetworkManager
    )
}