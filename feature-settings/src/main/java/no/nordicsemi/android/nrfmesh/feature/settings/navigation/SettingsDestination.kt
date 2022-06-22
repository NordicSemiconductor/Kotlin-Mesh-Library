package no.nordicsemi.android.nrfmesh.feature.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination

object SettingsDestination : MeshNavigationDestination {
    override val route: String = "settings_route"
    override val destination: String = "settings_destination"
}

fun NavGraphBuilder.nodesGraph() {
    composable(route = SettingsDestination.route) {

    }
}