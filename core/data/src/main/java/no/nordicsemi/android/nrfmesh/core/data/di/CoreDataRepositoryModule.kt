package no.nordicsemi.android.nrfmesh.core.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import no.nordicsemi.android.nrfmesh.core.common.di.DefaultDispatcher
import no.nordicsemi.android.nrfmesh.core.common.di.IoDispatcher
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.core.data.storage.SceneStatesDataStoreStorage
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object CoreDataRepositoryModule {

    @Singleton
    @Provides
    fun provideCoreDataRepository(
        preferences: DataStore<Preferences>,
        centralManager: CentralManager,
        meshNetworkManager: MeshNetworkManager,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
        @DefaultDispatcher defaultDispatcher: CoroutineDispatcher,
        storage: SceneStatesDataStoreStorage
    ) = CoreDataRepository(
        preferences = preferences,
        centralManager = centralManager,
        meshNetworkManager = meshNetworkManager,
        storage = storage,
        ioDispatcher = ioDispatcher,
        defaultDispatcher = defaultDispatcher
    )
}