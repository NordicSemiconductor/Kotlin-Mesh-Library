package no.nordicsemi.android.nrfmesh.destinations

import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.common.ui.scanner.model.DiscoveredBluetoothDevice
import no.nordicsemi.android.nrfmesh.ui.ProvisioningRoute
import no.nordicsemi.android.nrfmesh.viewmodel.ProvisioningViewModel

val provisioning = createDestination<DiscoveredBluetoothDevice, Unit>("provisioning")

val provisioningDestination = defineDestination(provisioning) {
    val viewModel: ProvisioningViewModel = hiltViewModel()
    ProvisioningRoute(viewModel = viewModel)
}
