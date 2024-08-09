package no.nordicsemi.android.nrfmesh.feature.network.keys.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.network.keys.NetworkKeysRoute
import no.nordicsemi.android.nrfmesh.feature.network.keys.NetworkKeysViewModel
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex

object NetworkKeysDestination : MeshNavigationDestination {
    override val route: String = "network_keys_route"
    override val destination: String = "network_keys_destination"
}

fun NavGraphBuilder.networkKeysGraph(
    onNavigateToKey: (KeyIndex) -> Unit,
    onBackPressed: () -> Unit,
) {
    composable(route = NetworkKeysDestination.route) {
        val viewModel = hiltViewModel<NetworkKeysViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        NetworkKeysRoute(
            uiState = uiState,
            navigateToKey = onNavigateToKey,
            onAddKeyClicked = viewModel::addNetworkKey,
            onSwiped = viewModel::onSwiped,
            onUndoClicked = viewModel::onUndoSwipe,
            remove = viewModel::remove
        )
    }
    networkKeyGraph(onBackPressed = onBackPressed)
}
