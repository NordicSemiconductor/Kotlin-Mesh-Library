package no.nordicsemi.android.nrfmesh.feature.scenes.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.scenes.ScenesRoute
import no.nordicsemi.android.nrfmesh.feature.scenes.ScenesViewModel

object ScenesDestination : MeshNavigationDestination {
    override val route: String = "scenes_route"
    override val destination: String = "scenes_destination"
}

fun NavGraphBuilder.scenesGraph(
    appState: AppState,
    onNavigateToScene: (MeshNavigationDestination, String) -> Unit,
    onBackPressed: () -> Unit
) {
    composable(route = ScenesDestination.route) {
        val viewModel = hiltViewModel<ScenesViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ScenesRoute(
            appState = appState,
            uiState = uiState,
            navigateToScene = { sceneNumber ->
                onNavigateToScene(
                    SceneDestination,
                    SceneDestination.createNavigationRoute(
                        sceneNumberArg = sceneNumber
                    )
                )
            },
            onAddSceneClicked = viewModel::addScene,
            onSwiped = viewModel::onSwiped,
            onUndoClicked = viewModel::onUndoSwipe,
            remove = viewModel::remove,
            onBackPressed = onBackPressed
        )
    }
    sceneGraph(appState = appState, onBackPressed = onBackPressed)
}