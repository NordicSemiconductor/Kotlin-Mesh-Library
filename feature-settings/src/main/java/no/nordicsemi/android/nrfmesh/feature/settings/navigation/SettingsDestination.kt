package no.nordicsemi.android.nrfmesh.feature.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.settings.SettingsRoute

object SettingsDestination : MeshNavigationDestination {
    override val route: String = "settings_route"
    override val destination: String = "settings_destination"
}

@Suppress("UNUSED_PARAMETER")
fun NavGraphBuilder.settingsGraph(
    navigateToProvisioners: () -> Unit,
    navigateToNetworkKeys: () -> Unit,
    navigateToApplicationKeys: () -> Unit,
    navigateToScenes: () -> Unit,
    navigateToExportNetwork: () -> Unit,
    nestedGraphs: NavGraphBuilder.() -> Unit
) {
    navigation(
        route = SettingsDestination.route,
        startDestination = SettingsDestination.destination
    ) {
        composable(route = SettingsDestination.destination) {
            SettingsRoute(
                navigateToProvisioners = navigateToProvisioners,
                navigateToNetworkKeys = navigateToNetworkKeys,
                navigateToApplicationKeys = navigateToApplicationKeys,
                navigateToScenes = navigateToScenes,
                navigateToExportNetwork = navigateToExportNetwork
            )
        }
        nestedGraphs()
    }
}