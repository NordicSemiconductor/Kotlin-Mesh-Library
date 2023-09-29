package no.nordicsemi.android.nrfmesh.destinations

import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResults
import no.nordicsemi.android.nrfmesh.ui.provisioning.ProvisioningRoute1
import no.nordicsemi.android.nrfmesh.viewmodel.ProvisioningViewModel

val provisioning = createDestination<BleScanResults, Unit>("provisioning")

val provisioningDestination = defineDestination(provisioning) {
    val viewModel: ProvisioningViewModel = hiltViewModel()
    ProvisioningRoute1(viewModel = viewModel)
}
