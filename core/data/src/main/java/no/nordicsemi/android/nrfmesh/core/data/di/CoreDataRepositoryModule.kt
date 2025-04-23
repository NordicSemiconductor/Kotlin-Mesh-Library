package no.nordicsemi.android.nrfmesh.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import no.nordicsemi.android.common.permissions.ble.bluetooth.BluetoothStateManager
import no.nordicsemi.android.common.permissions.ble.location.LocationStateManager
import no.nordicsemi.android.kotlin.ble.scanner.BleScanner
import no.nordicsemi.android.nrfmesh.core.common.dispatchers.di.DefaultDispatcher
import no.nordicsemi.android.nrfmesh.core.common.dispatchers.di.IoDispatcher
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object CoreDataRepositoryModule {

    @Singleton
    @Provides
    fun provideCoreDataRepository(
        @ApplicationContext context: Context,
        preferences: DataStore<Preferences>,
        bluetoothStateManager: BluetoothStateManager,
        locationStateManager: LocationStateManager,
        meshNetworkManager: MeshNetworkManager,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
        @DefaultDispatcher defaultDispatcher: CoroutineDispatcher
    ) = CoreDataRepository(
        context = context,
        preferences = preferences,
        bluetoothStateManager = bluetoothStateManager,
        locationStateManager = locationStateManager,
        scanner = BleScanner(context),
        meshNetworkManager = meshNetworkManager,
        ioDispatcher = ioDispatcher,
        defaultDispatcher = defaultDispatcher
    )
}