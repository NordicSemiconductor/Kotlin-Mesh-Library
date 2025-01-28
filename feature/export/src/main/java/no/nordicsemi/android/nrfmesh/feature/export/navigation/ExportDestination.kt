package no.nordicsemi.android.nrfmesh.feature.export.navigation

import android.os.Parcelable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.export.ExportRoute
import no.nordicsemi.android.nrfmesh.feature.export.ExportViewModel

@Parcelize
@Serializable
data object ApplicationKeysRoute : Parcelable

object ExportDestination : MeshNavigationDestination {
    override val route: String = "export_route"
    override val destination: String = "export_destination"
}

fun NavController.navigateToExport() { navigate(route = ExportDestination.route) }

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
