package no.nordicsemi.android.nrfmesh.feature.network.keys.navigation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.core.navigation.SettingsItemRoute
import no.nordicsemi.android.nrfmesh.feature.network.keys.NetworkKeysRoute
import no.nordicsemi.android.nrfmesh.feature.network.keys.NetworkKeysViewModel
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey

@Parcelize
data object NetworkKeysRoute : Parcelable

object NetworkKeysDestination : MeshNavigationDestination {
    override val route: String = "network_keys_route"
    override val destination: String = "network_keys_destination"
}

fun NavGraphBuilder.networkKeysGraph(
    appState: AppState,
    onNavigateToKey: (MeshNavigationDestination, String) -> Unit,
    onBackPressed: () -> Unit,
) {
    composable<SettingsItemRoute> {
        val viewModel = hiltViewModel<NetworkKeysViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    }
    // networkKeyGraph(appState = appState, onBackPressed = onBackPressed)
}

@Composable
fun NetworkKeysScreenRoute(
    appState: AppState,
    networkKeys: List<NetworkKey>,
    navigateToKey: (KeyIndex) -> Unit,
    onBackPressed: () -> Unit
) {
    val viewModel = hiltViewModel<NetworkKeysViewModel>()
    NetworkKeysRoute(
        appState = appState,
        networkKeys = networkKeys,
        navigateToKey = navigateToKey,
        onAddKeyClicked = viewModel::addNetworkKey,
        onSwiped = viewModel::onSwiped,
        onUndoClicked = viewModel::onUndoSwipe,
        remove = viewModel::remove,
        onBackPressed = onBackPressed
    )
}