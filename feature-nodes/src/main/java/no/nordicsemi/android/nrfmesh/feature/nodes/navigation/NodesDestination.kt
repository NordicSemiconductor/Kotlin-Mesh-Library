package no.nordicsemi.android.nrfmesh.feature.nodes.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination

object NodesDestination : MeshNavigationDestination {
    override val route: String = "nodes_route"
    override val destination: String = "nodes_destination"
}

fun NavGraphBuilder.nodesGraph() {
    composable(route = NodesDestination.route) {

    }
}