package no.nordicsemi.android.nrfmesh.feature.nodes.node

import android.os.Parcelable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import no.nordicsemi.kotlin.mesh.core.model.Node

@Parcelize
@Serializable
data class NodeRoute(val uuid: String) : Parcelable

fun NavController.navigateToNode(
    node: Node,
    navOptions: NavOptions? = null,
) = navigate(route = NodeRoute(uuid = node.uuid.toString()), navOptions = navOptions)

fun NavGraphBuilder.nodeGraph(navigateBack: () -> Unit) {
    composable<NodeRoute> {
        val viewModel = hiltViewModel<NodeViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        NodeListDetailsScreen(
            uiState = uiState,
            onRefresh = viewModel::onRefresh,
            onExcluded = viewModel::onExcluded,
            onItemSelected = viewModel::onItemSelected,
            send = viewModel::send,
            save = viewModel::save,
            navigateBack = navigateBack
        )
    }
}

