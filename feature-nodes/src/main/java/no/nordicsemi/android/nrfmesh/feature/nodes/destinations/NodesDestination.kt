package no.nordicsemi.android.nrfmesh.feature.nodes.destinations

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.common.navigation.createSimpleDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.nodes.NodesRoute
import no.nordicsemi.android.nrfmesh.feature.nodes.NodesScreenUiState
import no.nordicsemi.android.nrfmesh.feature.nodes.NodesViewModel

val nodes = createSimpleDestination("nodes")

val nodesDestination = defineDestination(nodes) {
    val viewModel: NodesViewModel = hiltViewModel()
    val uiState: NodesScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    NodesRoute(
        uiState = uiState,
        navigateToNode = viewModel::navigate,
        onSwiped = {},
        onUndoClicked = {},
        remove = {}
    )
}

val nodesDestinations = nodesDestination + nodeDestination + netKeysDestination + appKeysDestination

