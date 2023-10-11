package no.nordicsemi.android.nrfmesh.feature.proxy.destination

import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.common.navigation.createSimpleDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.proxy.ProxyFilterRoute
import no.nordicsemi.android.nrfmesh.feature.proxy.ProxyRouteViewModel

val proxyFilter = createSimpleDestination("proxy_filter")

val proxyFilterDestination = defineDestination(proxyFilter) {
    val viewModel: ProxyRouteViewModel = hiltViewModel()

    ProxyFilterRoute(viewModel = viewModel)
}

val proxyFilterDestinations = listOf(proxyFilterDestination)