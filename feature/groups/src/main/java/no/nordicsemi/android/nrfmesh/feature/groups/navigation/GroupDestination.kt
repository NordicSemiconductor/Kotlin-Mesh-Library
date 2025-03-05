package no.nordicsemi.android.nrfmesh.feature.groups.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.feature.groups.GroupListDetailScreen
import no.nordicsemi.android.nrfmesh.feature.groups.GroupViewModel
import no.nordicsemi.kotlin.data.HexString
import no.nordicsemi.kotlin.mesh.core.model.PrimaryGroupAddress

@Serializable
data class GroupRoute(val address: HexString)

fun NavController.navigateToGroup(address: PrimaryGroupAddress, navOptions: NavOptions? = null) =
    navigate(
        route = GroupRoute(address.toHexString()),
        navOptions = navOptions
    )

fun NavGraphBuilder.groupGraph(appState: AppState) {
    composable<GroupRoute> {
        val viewModel = hiltViewModel<GroupViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        GroupListDetailScreen(
            snackbarHostState = appState.snackbarHostState,
            messageState = uiState.messageState,
            uiState = uiState.groupState,
            onModelClicked = viewModel::onModelClicked,
            send = viewModel::send,
            deleteGroup = {
                if(viewModel.deleteGroup(group = it)){
                    appState.onBackPressed()
                }
            },
            save = viewModel::save
        )
    }
}