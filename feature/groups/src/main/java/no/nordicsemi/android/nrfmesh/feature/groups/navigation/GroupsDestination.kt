package no.nordicsemi.android.nrfmesh.feature.groups.navigation

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
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.feature.groups.GroupsRoute
import no.nordicsemi.android.nrfmesh.feature.groups.GroupsViewModel

@Serializable
@Parcelize
data object GroupsRoute : Parcelable

fun NavController.navigateToGroups(navOptions: NavOptions) = navigate(
    route = GroupsRoute,
    navOptions = navOptions
)

fun NavGraphBuilder.groupsGraph(appState: AppState) {
    composable<GroupsRoute> {
        val viewModel = hiltViewModel<GroupsViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        GroupsRoute(
            uiState = uiState,
            navigateToGroup = {},
            onSwiped = {},
            onUndoClicked = {},
            remove = {}
        )
    }
}