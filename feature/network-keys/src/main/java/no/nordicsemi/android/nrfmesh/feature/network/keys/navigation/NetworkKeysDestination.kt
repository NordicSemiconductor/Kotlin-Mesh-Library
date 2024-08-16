package no.nordicsemi.android.nrfmesh.feature.network.keys.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.network.keys.NetworkKeysRoute
import no.nordicsemi.android.nrfmesh.feature.network.keys.NetworkKeysViewModel

object NetworkKeysDestination : MeshNavigationDestination {
    override val route: String = "network_keys_route"
    override val destination: String = "network_keys_destination"
}

fun NavGraphBuilder.networkKeysGraph(
    appState: AppState,
    onNavigateToKey: (MeshNavigationDestination, String) -> Unit,
    onBackPressed: () -> Unit,
) {
    composable(route = NetworkKeysDestination.route) {
        val viewModel = hiltViewModel<NetworkKeysViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        NetworkKeysRoute(
            appState = appState,
            uiState = uiState,
            navigateToKey = { netKeyIndex ->
                onNavigateToKey(
                    NetworkKeyDestination,
                    NetworkKeyDestination.createNavigationRoute(
                        netKeyIndexArg = netKeyIndex
                    )
                )
            },
            onAddKeyClicked = viewModel::addNetworkKey,
            onSwiped = viewModel::onSwiped,
            onUndoClicked = viewModel::onUndoSwipe,
            remove = viewModel::remove,
            onBackPressed = onBackPressed
        )
    }
    networkKeyGraph(appState = appState, onBackPressed = onBackPressed)
}
