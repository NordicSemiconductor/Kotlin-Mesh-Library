package no.nordicsemi.android.nrfmesh.feature.nodes.destinations

import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.common.navigation.createSimpleDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.nodes.NodesRoute
import no.nordicsemi.android.nrfmesh.feature.nodes.NodesViewModel
import no.nordicsemi.android.nrfmesh.feature.scanner.destinations.scanner
import no.nordicsemi.android.nrfmesh.feature.scanner.destinations.scannerDestination

val nodes = createSimpleDestination("nodes")

val nodesDestination = defineDestination(nodes) {
    val viewModel: NodesViewModel = hiltViewModel()
    NodesRoute(viewModel = viewModel, navigateToScanner = {
        viewModel.navigateTo(scanner, it)
    })
}

val nodesDestinations = nodesDestination + nodeDestination + scannerDestination