package no.nordicsemi.android.feature.config.networkkeys.navigation

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.feature.config.networkkeys.ConfigNetKeysRoute
import no.nordicsemi.android.feature.config.networkkeys.ConfigNetKeysViewModel
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination.Companion.ARG
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeysDestination
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.networkKeysGraph
import java.util.UUID

object ConfigNetworkKeyDestination : MeshNavigationDestination {
    override val route: String = "config_net_key_route/{$ARG}"
    override val destination: String = "config_net_key_destination"

    /**
     * Creates destination route for a network key index.
     *
     * @param uuid UUID of the node to which the key is added
     */
    fun createNavigationRoute(uuid: UUID): String =
        "config_net_key_route/${Uri.encode(uuid.toString())}"
}

fun NavGraphBuilder.configNetworkKeysGraph(
    appState: AppState,
    onNavigateToDestination: (MeshNavigationDestination, String) -> Unit,
    onBackPressed: () -> Unit
) {
    composable(route = ConfigNetworkKeyDestination.route) {
        val viewmodel = hiltViewModel<ConfigNetKeysViewModel>()
        val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
        ConfigNetKeysRoute(
            appState = appState,
            uiState = uiState,
            navigateToNetworkKeys = {
                onNavigateToDestination(NetworkKeysDestination, NetworkKeysDestination.route)
            },
            onAddKeyClicked = viewmodel::addNetworkKey,
            onSwiped = viewmodel::onSwiped,
            resetMessageState = viewmodel::resetMessageState,
            onBackPressed = onBackPressed,
        )
    }
    networkKeysGraph(
        appState = appState,
        onNavigateToKey = onNavigateToDestination,
        onBackPressed = onBackPressed
    )
}

