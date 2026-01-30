package no.nordicsemi.android.nrfmesh.feature.export.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.nrfmesh.feature.export.ExportScreenContent
import no.nordicsemi.android.nrfmesh.feature.export.ExportViewModel

@Composable
fun ExportScreen(onDismissRequest: () -> Unit, onExportCompleted: (String) -> Unit) {
    val viewModel = hiltViewModel<ExportViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ExportScreenContent(
        uiState = uiState,
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