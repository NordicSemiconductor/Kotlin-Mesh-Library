package no.nordicsemi.android.nrfmesh.core.common.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.native
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CentralManagerModule {

    @Provides
    @Singleton
    fun provideCentralManager(
        @ApplicationContext context: Context,
        @DefaultDispatcher ioDispatcher: CoroutineDispatcher,
    ) = CentralManager.native(
        context = context,
        scope = CoroutineScope(context = SupervisorJob() + ioDispatcher)
    )
}