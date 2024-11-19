package no.nordicsemi.android.nrfmesh.feature.bind.appkeys.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination.Companion.ARG
import no.nordicsemi.android.nrfmesh.feature.bind.appkeys.BindAppKeysRoute
import no.nordicsemi.android.nrfmesh.feature.bind.appkeys.BindAppKeysViewModel
import no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.navigation.ConfigAppKeysDestination
import no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.navigation.configApplicationKeysGraph
import no.nordicsemi.kotlin.mesh.core.model.Model

object BoundAppKeysDestination : MeshNavigationDestination {
    const val MODEL_ID = "MODEL_ID"
    override val route: String
        get() = "bind_app_keys_route/{$ARG}/{$MODEL_ID}"
    override val destination: String = "bind_app_keys_destination"


    /**
     * Creates destination route for a network key index.
     *
     * @param model Model to navigate to.
     * @return The route string.
     */
    @Throws(IllegalStateException::class)
    fun createNavigationRoute(model: Model): String {
        val address = model.parentElement?.unicastAddress
            ?: throw IllegalStateException("Parent element address is null")
        return "bind_app_keys_route/${address.toHexString()}/${model.modelId.toHex()}"
    }

    /**
     * Creates destination route for publication configuration of a given model.
     *
     * @param appState      App state.
     * @param onBackPressed On back pressed callback.
     */
    fun NavGraphBuilder.bindAppKeysGraph(
        appState: AppState,
        onNavigateToDestination: (MeshNavigationDestination, String) -> Unit,
        onBackPressed: () -> Unit
    ) {
        composable(route = BoundAppKeysDestination.route) {
            val viewModel = hiltViewModel<BindAppKeysViewModel>()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            BindAppKeysRoute(
                appState = appState,
                uiState = uiState,
                send = viewModel::send,
                navigateToConfigApplicationKeys = {
                    onNavigateToDestination(
                        ConfigAppKeysDestination,
                        ConfigAppKeysDestination.createNavigationRoute(it)
                    )
                },
                onBackPressed = onBackPressed
            )
        }
        configApplicationKeysGraph(
            appState = appState,
            onNavigateToDestination = onNavigateToDestination,
            onBackPressed = onBackPressed
        )
    }
}