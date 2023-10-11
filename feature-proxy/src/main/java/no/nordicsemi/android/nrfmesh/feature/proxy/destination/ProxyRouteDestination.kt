package no.nordicsemi.android.nrfmesh.feature.proxy.destination

import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.common.navigation.createSimpleDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.proxy.ProxyRoute
import no.nordicsemi.android.nrfmesh.feature.proxy.viewmodel.ProxyViewModel

val proxy = createSimpleDestination("proxy")

val proxyDestination = defineDestination(proxy) {
    val viewModel: ProxyViewModel = hiltViewModel()

    ProxyRoute(
        onDeviceFound = { context, results ->
            viewModel.connect(context = context, results = results)
        }
    )
}

val proxyDestinations = listOf(proxyDestination)