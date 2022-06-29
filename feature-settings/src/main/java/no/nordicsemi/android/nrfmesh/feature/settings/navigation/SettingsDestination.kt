package no.nordicsemi.android.nrfmesh.feature.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.settings.SettingsScreen

object SettingsDestination : MeshNavigationDestination {
    override val route: String = "settings_route"
    override val destination: String = "settings_destination"
}

fun NavGraphBuilder.settingsGraph() {
    composable(route = SettingsDestination.route) {
        SettingsScreen()
    }
}