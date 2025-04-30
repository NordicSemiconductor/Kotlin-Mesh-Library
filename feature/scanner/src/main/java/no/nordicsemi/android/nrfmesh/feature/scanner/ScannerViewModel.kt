package no.nordicsemi.android.nrfmesh.feature.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * This class is responsible for managing the ui states of the scanner screen.
 *
 * @param isScanning True if the scanner is scanning.
 * @param scanningState The current scanning state.
 */
internal data class ScannerUiState(
    val isScanning: Boolean = false,
    val scanningState: ScanningState = ScanningState.Loading,
)

@HiltViewModel
internal class ScannerViewModel @Inject constructor(
    private val centralManager: CentralManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState = _uiState.asStateFlow()
    private var job: Job? = null

    init {
        println("ScannerViewModel init: $centralManager")
    }

    /**
     * Starts scanning for BLE devices.
     */
    @OptIn(ExperimentalUuidApi::class)
    internal fun startScanning(uuid: Uuid) {
        job?.cancel()
        job = centralManager.scan { ServiceUuid(uuid = uuid) }
            .onEach { scanResult ->
                val scanResults = _uiState.value.scanningState.let { state ->
                    if (state is ScanningState.ScanResultsDiscovered) state.results else emptyList()
                }

                // Check if the 2 existing result is already in the list.
                val isExistingResult = scanResults
                    .firstOrNull { it.peripheral.address == scanResult.peripheral.address }

                // Add the scan result to the list if it is not is already in the list, otherwise ignore it.
                if (isExistingResult == null) {
                    _uiState.update {
                        it.copy(
                            scanningState = ScanningState.ScanResultsDiscovered(
                                results = scanResults + scanResult
                            )
                        )
                    }
                }
            }
            // Update the scanning state when the scan is completed.
            .onCompletion {
                _uiState.update { it.copy(isScanning = false) }
            }
            .cancellable()
            .catch { e ->
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        scanningState = ScanningState.Error(e)
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Stop scanning.
     */
    internal fun stopScanning() {
        job?.cancel()
    }

    /**
     * Refresh the scanning process.
     */
    internal fun refreshScanning() {
        _uiState.update {
            it.copy(
                isScanning = true,
                scanningState = ScanningState.ScanResultsDiscovered(emptyList()),
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopScanning()
    }
}
