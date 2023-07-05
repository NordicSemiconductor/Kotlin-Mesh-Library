package no.nordicsemi.android.nrfmesh.core.storage.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import no.nordicsemi.android.nrfmesh.core.common.dispatchers.Dispatcher
import no.nordicsemi.android.nrfmesh.core.common.dispatchers.MeshDispatchers
import no.nordicsemi.android.nrfmesh.core.storage.MeshNetworkPropertiesStorage
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object MeshNetworkPropertiesStorageModule {

    @Singleton
    @Provides
    fun provideMeshNetworkPropertiesStorage(
        @ApplicationContext context: Context,
        @Dispatcher(MeshDispatchers.IO) ioDispatcher: CoroutineDispatcher
    ) = MeshNetworkPropertiesStorage(context = context, ioDispatcher = ioDispatcher)
}