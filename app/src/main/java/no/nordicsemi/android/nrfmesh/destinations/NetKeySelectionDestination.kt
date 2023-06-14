package no.nordicsemi.android.nrfmesh.destinations

import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.ui.provisioning.NetKeySelectorRoute
import no.nordicsemi.android.nrfmesh.viewmodel.NetKeySelectorViewModel

val netKeySelector = createDestination<Int, Int>("netKeySelection")

val netKeySelectorDestination = defineDestination(netKeySelector) {
    val viewModel: NetKeySelectorViewModel = hiltViewModel()
    NetKeySelectorRoute(viewModel = viewModel, onBackPressed = { keyIndex ->
        viewModel.navigateUpWithResult(netKeySelector, keyIndex.toInt())
    })
}
