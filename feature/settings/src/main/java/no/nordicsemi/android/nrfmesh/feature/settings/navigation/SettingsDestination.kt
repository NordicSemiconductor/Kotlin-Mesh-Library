package no.nordicsemi.android.nrfmesh.feature.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeysDestination
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.applicationKeysGraph
import no.nordicsemi.android.nrfmesh.feature.export.navigation.exportGraph
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeysDestination
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.networkKeysGraph
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.ProvisionersDestination
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.provisionersGraph
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.ScenesDestination
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.scenesGraph
import no.nordicsemi.android.nrfmesh.feature.settings.SettingsRoute

object SettingsDestination : MeshNavigationDestination {
    override val route: String = "settings_route"
    override val destination: String = "settings_destination"
}

fun NavGraphBuilder.settingsGraph(
    onNavigateToDestination: (MeshNavigationDestination, String) -> Unit,
    onBackPressed: () -> Unit,
) {
    composable(route = SettingsDestination.route) {
        SettingsRoute(
            navigateToProvisioners = {
                onNavigateToDestination(ProvisionersDestination, ProvisionersDestination.route)
            },
            navigateToNetworkKeys = {
                onNavigateToDestination(NetworkKeysDestination, NetworkKeysDestination.route)
            },
            navigateToApplicationKeys = {
                onNavigateToDestination(
                    ApplicationKeysDestination,
                    ApplicationKeysDestination.route
                )
            },
            navigateToScenes = {
                onNavigateToDestination(ScenesDestination, ScenesDestination.route)
            },
        )
    }
    exportGraph(onBackPressed = onBackPressed)
    provisionersGraph(
        onBackPressed = onBackPressed,
        onNavigateToDestination = onNavigateToDestination,
    )
    networkKeysGraph(
        onBackPressed = onBackPressed,
        onNavigateToKey = onNavigateToDestination
    )
    applicationKeysGraph(
        onBackPressed = onBackPressed,
        onNavigateToKey = onNavigateToDestination
    )
    scenesGraph(
        onBackPressed = onBackPressed,
        onNavigateToScene = onNavigateToDestination
    )
}