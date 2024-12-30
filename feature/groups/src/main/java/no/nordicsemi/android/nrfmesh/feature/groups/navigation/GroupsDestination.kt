package no.nordicsemi.android.nrfmesh.feature.groups.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.groups.GroupsRoute
import no.nordicsemi.android.nrfmesh.feature.groups.GroupsViewModel

@Serializable
data object GroupsRoute

@Serializable
data object GroupBaseRoute

object GroupsDestination : MeshNavigationDestination {
    override val route: String = "groups_route"
    override val destination: String = "groups_destination"
}

const val GROUPS_ROUTE = "groups_route"

fun NavController.navigateToGroups(navOptions: NavOptions) = navigate(
    route = GroupsDestination.route,
    navOptions = navOptions
)

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