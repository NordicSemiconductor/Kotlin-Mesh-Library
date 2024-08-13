package no.nordicsemi.android.nrfmesh.feature.groups.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.groups.GroupsRoute
import no.nordicsemi.android.nrfmesh.feature.groups.GroupsViewModel
import no.nordicsemi.kotlin.mesh.core.model.PrimaryGroupAddress

object GroupsDestination : MeshNavigationDestination {
    override val route: String = "groups_route"
    override val destination: String = "groups_destination"
}

fun NavGraphBuilder.groupsGraph(
    appState: AppState,
    onNavigateToDestination: (MeshNavigationDestination, String) -> Unit,
    onBackPressed: () -> Unit
) {
    composable(route = GroupsDestination.route) {
        val viewModel = hiltViewModel<GroupsViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        GroupsRoute(
            uiState = uiState,
            navigateToGroup = {
                onNavigateToDestination(GroupsDestination, GroupsDestination.route)
            },
            onSwiped = {},
            onUndoClicked = {},
            remove = {}
        )
    }
}