package no.nordicsemi.android.nrfmesh.feature.bind.appkeys.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination.Companion.ARG
import no.nordicsemi.android.nrfmesh.feature.bind.appkeys.BindAppKeysRoute
import no.nordicsemi.android.nrfmesh.feature.bind.appkeys.BindAppKeysViewModel

object BoundAppKeysDestination : MeshNavigationDestination {
    const val MODEL_ID = "MODEL_ID"
    override val route: String
        get() = "bind_app_keys_route/{$ARG}/{$MODEL_ID}"
    override val destination: String = "bind_app_keys_destination"

}

/*
fun NavGraphBuilder.bindAppKeysGraph(
    appState: AppState,
    onNavigateToDestination: (MeshNavigationDestination, String) -> Unit,
    onBackPressed: () -> Unit,
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
*/

@Composable
fun BindAppKeysScreenRoute(
    appState: AppState,
) {
    val viewModel = hiltViewModel<BindAppKeysViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BindAppKeysRoute(
        appState = appState,
        uiState = uiState,
        send = viewModel::send,
        navigateToConfigApplicationKeys = {

        },
        onBackPressed = {}
    )
}
