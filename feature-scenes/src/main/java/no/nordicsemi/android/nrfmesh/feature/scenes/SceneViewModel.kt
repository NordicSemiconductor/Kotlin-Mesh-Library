package no.nordicsemi.android.nrfmesh.feature.scenes

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.feature.scenes.destination.scene
import no.nordicsemi.kotlin.mesh.core.model.Scene
import no.nordicsemi.kotlin.mesh.core.model.SceneNumber
import javax.inject.Inject

@HiltViewModel
internal class SceneViewModel @Inject internal constructor(
    navigator: Navigator,
    savedStateHandle: SavedStateHandle,
    private val repository: CoreDataRepository
) : SimpleNavigationViewModel(navigator, savedStateHandle) {
    private val sceneNumberArg: SceneNumber = parameterOf(scene).toUShort()

    val _uiState = MutableStateFlow(SceneScreenUiState(SceneState.Loading))
    val uiState: StateFlow<SceneScreenUiState> = _uiState.asStateFlow()

    init {
        repository.network.onEach { meshNetwork ->
            Log.d("SceneViewModel", "meshNetwork.scenes: ${meshNetwork.scenes}")
            _uiState.update { state ->
                val scene = meshNetwork.scene(sceneNumberArg)
                when (val sceneState = state.sceneState) {
                    is SceneState.Loading -> SceneScreenUiState(
                        sceneState = SceneState.Success(
                            scene = scene
                        )
                    )
                    is SceneState.Success -> state.copy(sceneState = sceneState.copy(scene = scene))
                    else -> state
                }
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Invoked when the name of the application key is changed.
     *
     * @param name New application key name.
     */
    internal fun onNameChanged(name: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                val sceneState = state.sceneState as SceneState.Success
                val scene = sceneState.scene
                scene.name = name
                state.copy(sceneState = sceneState.copy(scene = scene))
            }
        }
        save()
    }

    /**
     * Saves the network.
     */
    private fun save() {
        viewModelScope.launch { repository.save() }
    }
}

sealed interface SceneState {
    data class Success(val scene: Scene) : SceneState
    data class Error(val throwable: Throwable) : SceneState
    data object Loading : SceneState
}

data class SceneScreenUiState internal constructor(
    val sceneState: SceneState = SceneState.Loading
)