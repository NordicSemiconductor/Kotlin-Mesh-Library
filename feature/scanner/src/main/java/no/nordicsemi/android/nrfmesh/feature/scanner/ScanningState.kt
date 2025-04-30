package no.nordicsemi.android.nrfmesh.feature.scanner

import no.nordicsemi.kotlin.ble.client.android.ScanResult

/** ScanningState represents the state of the scanning process. */
internal sealed interface ScanningState {

    /** Loading state. */
    data object Loading : ScanningState

    /** Devices discovered state.
     *
     * @param results The list of discovered devices.
     */
    data class ScanResultsDiscovered(val results: List<ScanResult>) : ScanningState

    /** Error state.
     *
     * @param error The error that occurred.
     */
    data class Error(val error: Throwable) : ScanningState
}