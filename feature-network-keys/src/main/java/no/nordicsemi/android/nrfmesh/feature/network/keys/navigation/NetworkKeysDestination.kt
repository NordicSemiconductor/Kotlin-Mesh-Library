package no.nordicsemi.android.nrfmesh.feature.network.keys.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.network.keys.NetworkKeysRoute

object ExportDestination : MeshNavigationDestination {
    override val route: String = "network_keys_route"
    override val destination: String = "network_keys_destination"
}

fun NavGraphBuilder.networkKeysGraph(
    snackbarHostState: SnackbarHostState,
    onBackPressed: () -> Unit
) {
    composable(route = ExportDestination.destination) {
        NetworkKeysRoute(snackbarHostState = snackbarHostState, onBackPressed = onBackPressed)
    }
}