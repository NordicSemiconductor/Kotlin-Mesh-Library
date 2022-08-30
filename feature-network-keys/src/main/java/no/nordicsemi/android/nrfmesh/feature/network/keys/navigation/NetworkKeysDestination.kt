package no.nordicsemi.android.nrfmesh.feature.network.keys.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.network.keys.NetworkKeysRoute
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex

object NetworkKeysDestination : MeshNavigationDestination {
    override val route: String = "network_keys_route"
    override val destination: String = "network_keys_destination"
}

fun NavGraphBuilder.networkKeysGraph(
    onBackPressed: () -> Unit,
    onNavigateToNetworkKey: (KeyIndex) -> Unit
) {
    composable(route = NetworkKeysDestination.route) {
        NetworkKeysRoute(
            navigateToNetworkKey = onNavigateToNetworkKey,
            onBackClicked = onBackPressed
        )
    }
    networkKeyGraph(onBackPressed = onBackPressed)
}