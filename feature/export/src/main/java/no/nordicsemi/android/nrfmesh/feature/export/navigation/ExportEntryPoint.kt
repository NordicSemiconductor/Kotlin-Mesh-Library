package no.nordicsemi.android.nrfmesh.feature.export.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.feature.export.ExportRoute
import no.nordicsemi.android.nrfmesh.feature.export.ExportViewModel

@Serializable
data object ExportKey : NavKey

@Composable
fun ExportScreenRoute(onDismissRequest: () -> Unit, onExportCompleted: (String) -> Unit) {
    val viewModel = hiltViewModel<ExportViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ExportRoute(
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