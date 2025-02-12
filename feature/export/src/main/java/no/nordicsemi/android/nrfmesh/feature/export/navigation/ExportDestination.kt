package no.nordicsemi.android.nrfmesh.feature.export.navigation

import android.os.Parcelable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.feature.export.ExportRoute
import no.nordicsemi.android.nrfmesh.feature.export.ExportViewModel

@Parcelize
@Serializable
data object ExportRoute : Parcelable

fun NavController.navigateToExport(navOptions: NavOptions? = null) = navigate(
    route = ExportRoute,
    navOptions = navOptions
)
fun NavGraphBuilder.exportGraph(
    appState: AppState,
    onBackPressed: () -> Unit
) {
    composable<ExportRoute> {
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
