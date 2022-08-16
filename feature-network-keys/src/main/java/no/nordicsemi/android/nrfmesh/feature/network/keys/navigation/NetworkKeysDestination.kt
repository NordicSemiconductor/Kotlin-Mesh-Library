package no.nordicsemi.android.nrfmesh.feature.network.keys.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.network.keys.NetworkKeysRoute

object NetworkKeysDestination : MeshNavigationDestination {
    override val route: String = "network_keys_route"
    override val destination: String = "network_keys_destination"
}

fun NavGraphBuilder.networkKeysGraph(onBackPressed: () -> Unit) {
    composable(route = NetworkKeysDestination.route) {
        NetworkKeysRoute(onBackPressed = onBackPressed)
    }
}