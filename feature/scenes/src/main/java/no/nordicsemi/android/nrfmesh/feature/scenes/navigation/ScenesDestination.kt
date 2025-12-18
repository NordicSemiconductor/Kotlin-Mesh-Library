package no.nordicsemi.android.nrfmesh.feature.scenes.navigation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.feature.scenes.ScenesRoute
import no.nordicsemi.android.nrfmesh.feature.scenes.ScenesViewModel
import no.nordicsemi.kotlin.mesh.core.model.SceneNumber

@Serializable
@Parcelize
data object ScenesContent : Parcelable

@Composable
fun ScenesScreenRoute(
    snackbarHostState: SnackbarHostState,
    highlightSelectedItem: Boolean,
    onSceneClicked: (SceneNumber) -> Unit,
    navigateToScene: (SceneNumber) -> Unit,
    navigateUp: () -> Unit,
) {
    val viewModel = hiltViewModel<ScenesViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ScenesRoute(
        snackbarHostState = snackbarHostState,
        highlightSelectedItem = highlightSelectedItem,
        selectedSceneNumber = uiState.selectedSceneNumber,
        scenes = uiState.scenes,
        onAddSceneClicked = viewModel::addScene,
        onSceneClicked = {
            viewModel.selectScene(number = it)
            onSceneClicked(it)
        },
        navigateToScene = {
            viewModel.selectScene(it)
            navigateToScene(it)
        },
        onSwiped = {
            viewModel.onSwiped(scene = it)
            if(uiState.selectedSceneNumber == it.number) {
                navigateUp()
            }
        },
        onUndoClicked = viewModel::onUndoSwipe,
        remove = viewModel::remove
    )
}