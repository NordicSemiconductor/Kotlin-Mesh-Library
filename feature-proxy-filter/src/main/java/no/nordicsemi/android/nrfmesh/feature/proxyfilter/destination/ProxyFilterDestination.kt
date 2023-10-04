package no.nordicsemi.android.nrfmesh.feature.proxyfilter.destination

import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.common.navigation.createSimpleDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.proxyfilter.ProxyFilterRoute
import no.nordicsemi.android.nrfmesh.feature.proxyfilter.ProxyFilterViewModel

val proxyFilter = createSimpleDestination("proxy_filter")

val proxyFilterDestination = defineDestination(proxyFilter) {
    val viewModel: ProxyFilterViewModel = hiltViewModel()

    ProxyFilterRoute(viewModel = viewModel)
}

val proxyFilterDestinations = listOf(proxyFilterDestination)