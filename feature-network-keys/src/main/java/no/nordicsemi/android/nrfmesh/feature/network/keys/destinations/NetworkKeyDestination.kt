@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.network.keys.destinations

import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.network.keys.NetworkKeyRoute
import no.nordicsemi.android.nrfmesh.feature.network.keys.NetworkKeyViewModel

val networkKey = createDestination<Int, Unit>("network_key")

val networkKeyDestination = defineDestination(networkKey) {
    val viewModel: NetworkKeyViewModel = hiltViewModel()

    NetworkKeyRoute(viewModel = viewModel, onBackPressed = {})
}