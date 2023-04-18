package no.nordicsemi.android.nrfmesh.feature.scanner.destinations

import android.os.ParcelUuid
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.common.ui.scanner.ScannerView
import no.nordicsemi.android.common.ui.scanner.model.DiscoveredBluetoothDevice

val scanner = createDestination<ParcelUuid, DiscoveredBluetoothDevice>("scanner")

val scannerDestination = defineDestination(scanner) {
    val viewModel: SimpleNavigationViewModel = hiltViewModel()

    val uuid = viewModel.parameterOf(scanner)

    ScannerView(
        uuid = uuid,
        onResult = { viewModel.navigateUpWithResult(scanner, it) }
    )
}