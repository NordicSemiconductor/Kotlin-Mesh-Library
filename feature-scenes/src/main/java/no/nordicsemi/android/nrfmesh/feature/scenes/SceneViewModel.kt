package no.nordicsemi.android.nrfmesh.feature.scenes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import no.nordicsemi.kotlin.mesh.core.model.Scene
import javax.inject.Inject

@HiltViewModel
internal class SceneViewModel @Inject internal constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: DataStoreRepository
) : ViewModel() {
    private lateinit var scene: Scene
    private val sceneNumberArg: String = "0"
        // checkNotNull(savedStateHandle[SceneDestination.sceneNumberArg])

    val uiState: StateFlow<SceneScreenUiState> = repository.network.map { network ->
        this@SceneViewModel.scene = network.scene(sceneNumberArg.toUShort())
        SceneScreenUiState(
            sceneState = SceneState.Success(scene = scene)
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SceneScreenUiState(SceneState.Loading)
    )

    /**
     * Invoked when the name of the application key is changed.
     *
     * @param name New application key name.
     */
    internal fun onNameChanged(name: String) {
        if (scene.name != name) {
            scene.name = name
            save()
        }
    }

    /**
     * Saves the network.
     */
    internal fun save() {
        viewModelScope.launch { repository.save() }
    }
}

sealed interface SceneState {
    data class Success(val scene: Scene) : SceneState
    data class Error(val throwable: Throwable) : SceneState
    object Loading : SceneState
}

@Suppress("ArrayInDataClass")
data class SceneScreenUiState internal constructor(
    val sceneState: SceneState = SceneState.Loading
)