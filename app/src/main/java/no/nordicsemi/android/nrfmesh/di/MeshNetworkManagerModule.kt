package no.nordicsemi.android.nrfmesh.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MeshNetworkManagerModule {

    @Provides
    @Singleton
    fun provideMeshManager() = MeshNetworkManager()
}