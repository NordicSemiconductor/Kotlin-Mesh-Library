package no.nordicsemi.android.nrfmesh.core.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import no.nordicsemi.android.nrfmesh.core.data.storage.MeshNetworkStorage

@InstallIn(ActivityRetainedComponent::class)
@Module
object MeshNetworkStorageModule {
    @ActivityRetainedScoped
    @Provides
    fun provideMeshNetworkStorage(
        @ApplicationContext context: Context
    ): MeshNetworkStorage {
        return MeshNetworkStorage(context = context)
    }
}