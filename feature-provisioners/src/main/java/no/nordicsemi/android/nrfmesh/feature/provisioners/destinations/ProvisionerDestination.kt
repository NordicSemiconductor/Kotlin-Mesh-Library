@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.provisioners.destinations

import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.provisioners.ProvisionerRoute
import no.nordicsemi.android.nrfmesh.feature.provisioners.ProvisionerViewModel
import java.util.*

val provisioner = createDestination<UUID, Unit>("provisioner")

val provisionerDestination = defineDestination(provisioner) {
    val viewModel: ProvisionerViewModel = hiltViewModel()

    ProvisionerRoute(
        viewModel = viewModel,
        navigateToUnicastRanges = { viewModel.navigateTo(unicastRanges, it) },
        navigateToGroupRanges = { viewModel.navigateTo(groupRanges, it) },
        navigateToSceneRanges = { viewModel.navigateTo(sceneRanges, it) }
    )
}