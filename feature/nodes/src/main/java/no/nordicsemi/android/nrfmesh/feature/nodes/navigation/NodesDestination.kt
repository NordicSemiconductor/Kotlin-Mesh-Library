package no.nordicsemi.android.nrfmesh.feature.nodes.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.feature.nodes.NodesRoute
import no.nordicsemi.android.nrfmesh.feature.nodes.NodesViewModel
import no.nordicsemi.android.nrfmesh.feature.nodes.node.navigation.navigateToNode
import no.nordicsemi.android.nrfmesh.feature.nodes.node.navigation.nodeGraph

@Serializable
data object NodesBaseRoute

@Serializable
data object NodesRoute

fun NavController.navigateToNodes(navOptions: NavOptions) = navigate(
    route = NodesRoute,
    navOptions = navOptions
)

fun NavGraphBuilder.nodesGraph(appState: AppState, navigateBack: () -> Unit) {
    navigation<NodesBaseRoute>(startDestination = NodesRoute) {
        composable<NodesRoute> {
            val viewModel = hiltViewModel<NodesViewModel>()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            NodesRoute(
                appState = appState,
                uiState = uiState,
                navigateToNode = { appState.navController.navigateToNode(node = it) },
                onSwiped = { },
                onUndoClicked = { },
                remove = { }
            )
        }
        nodeGraph(appState = appState, navigateBack = navigateBack)
    }
}