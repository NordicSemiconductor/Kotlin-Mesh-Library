package no.nordicsemi.android.nrfmesh.feature.settings.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavController
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
    modifier: Modifier,
    navController: NavController,
    navigateToProvisioners: (String) -> Unit,
    navigateToNetworkKeys: (String) -> Unit,
    navigateToApplicationKeys: (String) -> Unit,
    navigateToScenes: (String) -> Unit,
    navigateToIvIndex: (String) -> Unit,
    nestedGraphs: NavGraphBuilder.() -> Unit
) {
    navigation(
        route = SettingsDestination.route,
        startDestination = SettingsDestination.destination
    ) {
        composable(route = SettingsDestination.destination) {
            SettingsRoute(navController = navController)
        }
        nestedGraphs()
    }
}