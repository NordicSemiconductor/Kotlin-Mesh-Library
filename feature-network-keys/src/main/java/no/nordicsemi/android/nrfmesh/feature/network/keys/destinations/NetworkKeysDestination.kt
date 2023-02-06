package no.nordicsemi.android.nrfmesh.feature.network.keys.destinations

import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.network.keys.NetworkKeysRoute
import no.nordicsemi.android.nrfmesh.feature.network.keys.NetworkKeysViewModel

val networkKeys = createDestination<Unit, Unit>("network_keys")

val networkKeysDestination = defineDestination(networkKeys) {
    val viewModel: NetworkKeysViewModel = hiltViewModel()
    NetworkKeysRoute(
        viewModel = viewModel,
        navigateToKey = { viewModel.navigate(networkKey, it.toInt()) }
    )
}

val networkKeysDestinations = networkKeysDestination + networkKeyDestination