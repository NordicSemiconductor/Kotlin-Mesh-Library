package no.nordicsemi.android.nrfmesh.feature.groups.navigation

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
import no.nordicsemi.android.nrfmesh.feature.groups.GroupsRoute
import no.nordicsemi.android.nrfmesh.feature.groups.GroupsViewModel

@Serializable
data object GroupsBaseRoute

@Serializable
data object GroupsRoute

fun NavController.navigateToGroups(navOptions: NavOptions? = null) = navigate(
    route = GroupsRoute,
    navOptions = navOptions
)

fun NavGraphBuilder.groupsGraph(appState: AppState) {
    navigation<GroupsBaseRoute>(startDestination = GroupsRoute) {
        composable<GroupsRoute> {
            val viewModel = hiltViewModel<GroupsViewModel>()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            GroupsRoute(
                uiState = uiState,
                navigateToGroup = { appState.navController.navigateToGroup(address = it) }
            )
        }
        groupGraph(appState = appState)
    }
}