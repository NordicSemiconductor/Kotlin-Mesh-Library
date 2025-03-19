package no.nordicsemi.android.nrfmesh.feature.nodes.node.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.feature.nodes.node.NodeListDetailsScreen
import no.nordicsemi.android.nrfmesh.feature.nodes.node.NodeViewModel
import no.nordicsemi.kotlin.mesh.core.model.Node
import java.util.UUID

@Serializable
data class NodeRoute(val uuid: String)

fun NavController.navigateToNode(node: Node, navOptions: NavOptions? = null) = navigateToNode(
    uuid = node.uuid, navOptions = navOptions
)

fun NavController.navigateToNode(uuid: UUID, navOptions: NavOptions? = null) = navigate(
    route = NodeRoute(uuid = uuid.toString()), navOptions = navOptions
)

fun NavGraphBuilder.nodeGraph(appState: AppState, navigateBack: () -> Unit) {
    composable<NodeRoute> {
        val viewModel = hiltViewModel<NodeViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        NodeListDetailsScreen(
            uiState = uiState,
            onRefresh = viewModel::onRefresh,
            onExcluded = viewModel::onExcluded,
            onItemSelected = viewModel::onItemSelected,
            send = viewModel::send,
            requestNodeIdentityStates = viewModel::requestNodeIdentityStates,
            save = viewModel::save,
            resetMessageState = viewModel::resetMessageState,
            appState = appState,
            navigateBack = navigateBack
        )
    }
}

