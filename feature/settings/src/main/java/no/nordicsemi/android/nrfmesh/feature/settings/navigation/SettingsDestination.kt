package no.nordicsemi.android.nrfmesh.feature.settings.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeysDestination
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.applicationKeysGraph
import no.nordicsemi.android.nrfmesh.feature.export.navigation.ExportDestination
import no.nordicsemi.android.nrfmesh.feature.export.navigation.exportGraph
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeysDestination
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.networkKeysGraph
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.ProvisionersDestination
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.provisionersGraph
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.ScenesDestination
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.scenesGraph
import no.nordicsemi.android.nrfmesh.feature.settings.SettingsRoute
import no.nordicsemi.android.nrfmesh.feature.settings.SettingsViewModel

object SettingsDestination : MeshNavigationDestination {
    override val route: String = "settings_route"
    override val destination: String = "settings_destination"
}

fun NavGraphBuilder.settingsGraph(
    appState: AppState,
    onNavigateToDestination: (MeshNavigationDestination, String) -> Unit,
    onBackPressed: () -> Unit,
) {
    composable(route = SettingsDestination.route) {
        val viewModel = hiltViewModel<SettingsViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        SettingsRoute(
            appState = appState,
            uiState = uiState,
            onNameChanged = viewModel::onNameChanged,
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
            importNetwork = { uri, contentResolver ->
                viewModel.importNetwork(uri = uri, contentResolver = contentResolver)
            },
            navigateToExport = {
                onNavigateToDestination(ExportDestination, ExportDestination.route)
            },
            resetNetwork = viewModel::resetNetwork
        )
    }
    exportGraph(appState = appState, onBackPressed = onBackPressed)
    provisionersGraph(
        appState = appState,
        onBackPressed = onBackPressed,
        onNavigateToDestination = onNavigateToDestination,
    )
    networkKeysGraph(
        appState = appState,
        onBackPressed = onBackPressed,
        onNavigateToKey = onNavigateToDestination
    )
    applicationKeysGraph(
        appState = appState,
        onBackPressed = onBackPressed,
        onNavigateToKey = onNavigateToDestination
    )
    scenesGraph(
        appState = appState,
        onBackPressed = onBackPressed,
        onNavigateToScene = onNavigateToDestination
    )
}