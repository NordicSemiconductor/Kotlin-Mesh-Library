package no.nordicsemi.android.nrfmesh.feature.export.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.nrfmesh.feature.export.ExportRoute
import no.nordicsemi.android.nrfmesh.feature.export.ExportViewModel

@Composable
fun ExportScreenRoute(onDismissRequest: () -> Unit, onExportCompleted: (String) -> Unit, ) {
    val viewModel = hiltViewModel<ExportViewModel>()
    ExportRoute(
        uiState = viewModel.uiState,
        onExportOptionSelected = viewModel::onExportOptionSelected,
        onNetworkKeySelected = viewModel::onNetworkKeySelected,
        onProvisionerSelected = viewModel::onProvisionerSelected,
        onExportDeviceKeysToggled = viewModel::onExportDeviceKeysToggled,
        export = { contentResolver, uri ->
            viewModel.export(contentResolver = contentResolver, uri = uri)
            onDismissRequest()
        },
        onExportStateDisplayed = viewModel::onExportStateDisplayed,
        onExportCompleted = onExportCompleted
    )
}
