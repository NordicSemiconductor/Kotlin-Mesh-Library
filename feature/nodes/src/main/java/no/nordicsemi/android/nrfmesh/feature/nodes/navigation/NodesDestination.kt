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
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.nodes.NodesRoute
import no.nordicsemi.android.nrfmesh.feature.nodes.NodesViewModel
import no.nordicsemi.android.nrfmesh.feature.provisioning.navigation.provisioningGraph

@Serializable
data object NodesRoute

@Serializable
data object NodesBaseRoute

fun NavController.navigateToNodes(navOptions: NavOptions) = navigate(
    route = NodesRoute,
    navOptions = navOptions
)

fun NavGraphBuilder.nodesGraph(
    appState: AppState,
    onNavigateToDestination: (MeshNavigationDestination, String) -> Unit,
    onBackPressed: () -> Unit
) {
    navigation<NodesBaseRoute>(startDestination = NodesRoute) {
        composable<NodesRoute> {
            val viewModel = hiltViewModel<NodesViewModel>()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            NodesRoute(
                appState = appState,
                uiState = uiState,
                navigateToNode = { node ->
                    onNavigateToDestination(
                        NodeDestination,
                        NodeDestination.createNavigationRoute(node.uuid)
                    )
                },
                onSwiped = { },
                onUndoClicked = { },
                remove = { }
            )
        }
        provisioningGraph(
            appState = appState,
            onNavigateToDestination = onNavigateToDestination,
            onBackPressed = onBackPressed
        )
        nodeGraph(
            appState = appState,
            onNavigateToDestination = onNavigateToDestination,
            onBackPressed = onBackPressed
        )
    }
}