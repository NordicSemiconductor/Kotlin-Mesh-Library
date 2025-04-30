package no.nordicsemi.android.nrfmesh.feature.scanner.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.nrfmesh.feature.scanner.ScannerScreen
import no.nordicsemi.android.nrfmesh.feature.scanner.ScannerViewModel
import no.nordicsemi.kotlin.ble.client.android.ScanResult
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@OptIn(ExperimentalUuidApi::class)
@Composable
fun ScannerScreenRoute(uuid: Uuid, onScanResultSelected: (ScanResult) -> Unit) {
    val viewModel = hiltViewModel<ScannerViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ScannerScreen(
        uuid = uuid,
        uiState = uiState,
        startScanning = viewModel::startScanning,
        onRefreshScan = viewModel::refreshScanning,
        onScanResultSelected = {
            viewModel.stopScanning()
            onScanResultSelected(it)
        }
    )
}