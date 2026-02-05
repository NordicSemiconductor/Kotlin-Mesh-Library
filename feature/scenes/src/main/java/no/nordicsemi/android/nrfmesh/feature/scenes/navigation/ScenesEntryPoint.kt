package no.nordicsemi.android.nrfmesh.feature.scenes.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.Navigator
import no.nordicsemi.android.nrfmesh.core.navigation.SettingsListDetailSceneKey
import no.nordicsemi.android.nrfmesh.feature.scenes.ScenesScreen
import no.nordicsemi.android.nrfmesh.feature.scenes.ScenesViewModel
import no.nordicsemi.android.nrfmesh.feature.scenes.scene.navigation.SceneContentKey
import no.nordicsemi.android.nrfmesh.feature.scenes.scene.navigation.sceneEntry

@Serializable
data object ScenesContentKey : NavKey

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.scenesEntry(appState: AppState, navigator: Navigator) {
    entry<ScenesContentKey>(
        metadata = ListDetailSceneStrategy.detailPane(
            sceneKey = SettingsListDetailSceneKey
        )
    ) {
        val viewModel = hiltViewModel<ScenesViewModel, ScenesViewModel.Factory>(
            key = "ScenesViewModel"
        ) { factory ->
            factory.create()
        }
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ScenesScreen(
            snackbarHostState = appState.snackbarHostState,
            highlightSelectedItem = false,
            selectedSceneNumber = uiState.selectedSceneNumber,
            scenes = uiState.scenes,
            onAddSceneClicked = viewModel::addScene,
            onSceneClicked = {
                viewModel.selectScene(number = it)
                navigator.navigate(key = SceneContentKey(number = it))
            },
            navigateToScene = {
                viewModel.selectScene(it)
                navigator.navigate(key = SceneContentKey(number = it))
            },
            onSwiped = {
                viewModel.onSwiped(scene = it)
                if (uiState.selectedSceneNumber == it.number) {
                    navigator.goBack()
                }
            },
            onUndoClicked = viewModel::onUndoSwipe,
            remove = viewModel::remove
        )
    }
    sceneEntry(appState = appState)
}