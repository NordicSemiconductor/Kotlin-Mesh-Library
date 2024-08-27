@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.provisioners.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.provisioners.ProvisionersRoute
import no.nordicsemi.android.nrfmesh.feature.provisioners.ProvisionersViewModel

object ProvisionersDestination : MeshNavigationDestination {
    override val route: String = "provisioners_route"
    override val destination: String = "provisioners_destination"
}

fun NavGraphBuilder.provisionersGraph(
    appState: AppState,
    onNavigateToDestination: (MeshNavigationDestination, String) -> Unit,
    onBackPressed: () -> Unit
) {
    composable(route = ProvisionersDestination.route) {
        val viewModel = hiltViewModel<ProvisionersViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ProvisionersRoute(
            appState = appState,
            uiState = uiState,
            navigateToProvisioner = { provisionerUuid ->
                onNavigateToDestination(
                    ProvisionerDestination,
                    ProvisionerDestination.createNavigationRoute(provisionerUuid)
                )
            },
            onAddProvisionerClicked = viewModel::addProvisioner,
            onSwiped = viewModel::onSwiped,
            onUndoClicked = viewModel::onUndoSwipe,
            remove = viewModel::remove,
            onBackPressed = onBackPressed
        )
    }
    provisionerGraph(
        appState = appState,
        onNavigateToUnicastRanges = onNavigateToDestination,
        onNavigateToGroupRanges = onNavigateToDestination,
        onNavigateToSceneRanges = onNavigateToDestination,
        onBackPressed = onBackPressed
    )
}