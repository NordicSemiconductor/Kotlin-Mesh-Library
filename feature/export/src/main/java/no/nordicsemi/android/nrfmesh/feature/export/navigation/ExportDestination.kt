package no.nordicsemi.android.nrfmesh.feature.export.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.export.ExportRoute
import no.nordicsemi.android.nrfmesh.feature.export.ExportViewModel

object ExportDestination : MeshNavigationDestination {
    override val route: String = "export_route"
    override val destination: String = "export_destination"
}

fun NavGraphBuilder.exportGraph(
    appState: AppState,
    onBackPressed: () -> Unit
) {
    composable(route = ExportDestination.route) {
        val viewModel = hiltViewModel<ExportViewModel>()
        ExportRoute(
            appState = appState,
            uiState = viewModel.uiState,
            onExportEverythingToggled = viewModel::onExportEverythingToggled,
            onNetworkKeySelected = viewModel::onNetworkKeySelected,
            onProvisionerSelected = viewModel::onProvisionerSelected,
            onExportDeviceKeysToggled = viewModel::onExportDeviceKeysToggled,
            onExportClicked = viewModel::export,
            onExportStateDisplayed = viewModel::onExportStateDisplayed,
            onBackPressed = onBackPressed
        )
    }
}
