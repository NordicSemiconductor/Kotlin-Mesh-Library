package no.nordicsemi.android.nrfmesh.feature.scenes.navigation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.scenes.ScenesRoute
import no.nordicsemi.android.nrfmesh.feature.scenes.ScenesViewModel
import no.nordicsemi.kotlin.mesh.core.model.SceneNumber

@Parcelize
data object ScenesRoute : Parcelable

object ScenesDestination : MeshNavigationDestination {
    override val route: String = "scenes_route"
    override val destination: String = "scenes_destination"
}

@Composable
fun ScenesScreenRoute(
    highlightSelectedItem: Boolean,
    navigateToScene: (SceneNumber) -> Unit,
    navigateUp: () -> Unit,
) {
    val viewModel = hiltViewModel<ScenesViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ScenesRoute(
        highlightSelectedItem = highlightSelectedItem,
        scenes = uiState.scenes,
        onAddSceneClicked = viewModel::addScene,
        navigateToScene = {
            viewModel.selectScene(it)
            navigateToScene(it)
        },
        onSwiped = {
            viewModel.onSwiped(it)
            if(viewModel.isCurrentlySelectedScene(it.number)) {
                navigateUp()
            }
        },
        onUndoClicked = viewModel::onUndoSwipe,
        remove = viewModel::remove
    )
}