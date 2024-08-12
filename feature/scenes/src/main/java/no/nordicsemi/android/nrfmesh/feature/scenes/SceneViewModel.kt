package no.nordicsemi.android.nrfmesh.feature.scenes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.SceneDestination
import no.nordicsemi.kotlin.mesh.core.model.Scene
import no.nordicsemi.kotlin.mesh.core.model.SceneNumber
import javax.inject.Inject

@HiltViewModel
internal class SceneViewModel @Inject internal constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CoreDataRepository
) : ViewModel() {
    private val sceneNumberArg: SceneNumber =
        checkNotNull(savedStateHandle[SceneDestination.sceneNumberArg])

    private val _uiState = MutableStateFlow(SceneScreenUiState(SceneState.Loading))
    val uiState: StateFlow<SceneScreenUiState> = _uiState.asStateFlow()

    init {
        repository.network.onEach { meshNetwork ->
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
                sceneState.scene.name = name
                state.copy(sceneState = sceneState)
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