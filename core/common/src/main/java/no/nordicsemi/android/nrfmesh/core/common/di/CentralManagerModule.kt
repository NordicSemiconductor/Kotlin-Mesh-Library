package no.nordicsemi.android.nrfmesh.core.common.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.ActivityRetainedLifecycle
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.lifecycle.RetainedLifecycle
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.native
import javax.inject.Singleton

@Module
@InstallIn(ActivityRetainedComponent::class)
object CentralManagerModule {

    @Provides
    @ActivityRetainedScoped
    fun provideCentralManager(
        @ApplicationContext context: Context,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
        lifecycle: ActivityRetainedLifecycle,
    ) = CentralManager.native(
        context = context,
        scope = CoroutineScope(context = SupervisorJob() + ioDispatcher)
    ).also {
        // Close the manager when the lifecycle is destroyed
        // Also clear the coroutine scope
        lifecycle.addOnClearedListener {
            it.close()
            ioDispatcher.cancel()
        }
    }
}