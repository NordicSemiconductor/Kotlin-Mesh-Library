package no.nordicsemi.android.nrfmesh.feature.groups.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination

object GroupsDestination : MeshNavigationDestination {
    override val route: String = "groups_route"
    override val destination: String = "groups_destination"
}

fun NavGraphBuilder.groupsGraph() {
    composable(route = GroupsDestination.route) {

    }
}