@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.provisioners.destinations

import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.provisioners.ProvisionersRoute
import no.nordicsemi.android.nrfmesh.feature.provisioners.ProvisionersViewModel
import java.util.*

val provisioners = createDestination<Unit, Unit>("provisioners")

val provisionersDestination = defineDestination(provisioners) {
    val viewModel: ProvisionersViewModel = hiltViewModel()
    ProvisionersRoute(
        viewModel = viewModel,
        navigateToProvisioner = { viewModel.navigate(provisioner, it) },
        onBackClicked = {})
}

val provisionersDestinations = provisionersDestination + provisionerDestination