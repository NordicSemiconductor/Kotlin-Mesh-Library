package no.nordicsemi.android.nrfmesh.core.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import no.nordicsemi.android.nrfmesh.core.data.storage.MeshNetworkStorage
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object MeshNetworkStorageModule {
    @Singleton
    @Provides
    fun provideMeshNetworkStorage(
        @ApplicationContext context: Context
    ): MeshNetworkStorage {
        return MeshNetworkStorage(context = context)
    }

}