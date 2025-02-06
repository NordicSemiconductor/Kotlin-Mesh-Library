package no.nordicsemi.android.nrfmesh.feature.nodes.navigation

import android.os.Parcelable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.feature.nodes.NodesRoute
import no.nordicsemi.android.nrfmesh.feature.nodes.NodesViewModel
import no.nordicsemi.android.nrfmesh.feature.nodes.node.navigateToNode
import no.nordicsemi.android.nrfmesh.feature.nodes.node.nodeGraph

@Serializable
@Parcelize
data object NodesRoute : Parcelable

@Serializable
@Parcelize
data object NodesBaseRoute : Parcelable

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
        nodeGraph(navigateBack = navigateBack)
    }
}