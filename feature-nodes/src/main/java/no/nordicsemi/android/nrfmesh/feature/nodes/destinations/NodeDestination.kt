package no.nordicsemi.android.nrfmesh.feature.nodes.destinations

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.nodes.NodeRoute
import no.nordicsemi.android.nrfmesh.feature.nodes.NodeViewModel
import java.util.UUID

val node = createDestination<UUID, Unit>("node")

val nodeDestination = defineDestination(node) {
    val viewModel: NodeViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NodeRoute(
        uiState = uiState,
        onRefresh = viewModel::onRefresh,
        onNameChanged = viewModel::onNameChanged,
        onNetworkKeysClicked = viewModel::onNetworkKeysClicked,
        onApplicationKeysClicked = {},
        onElementsClicked = {},
        onGetTtlClicked = {},
        onProxyStateToggled = viewModel::onProxyStateToggled,
        onGetProxyStateClicked = viewModel::onGetProxyStateClicked,
        onExcluded = {},
        onResetClicked = viewModel::onResetClicked,
    )
}