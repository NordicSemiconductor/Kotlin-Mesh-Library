package no.nordicsemi.android.nrfmesh.feature.scenes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
    private lateinit var selectedScene: Scene
    private val sceneNumberArg: SceneNumber = parameterOf(scene).toUShort()

    val uiState: StateFlow<SceneScreenUiState> = repository.network.map { network ->
        this@SceneViewModel.selectedScene = network.scene(sceneNumberArg)
        SceneScreenUiState(
            sceneState = SceneState.Success(scene = selectedScene)
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SceneScreenUiState(SceneState.Loading)
    )

    override fun onCleared() {
        super.onCleared()
        save()
    }

    /**
     * Invoked when the name of the application key is changed.
     *
     * @param name New application key name.
     */
    internal fun onNameChanged(name: String) {
        if (selectedScene.name != name) {
            selectedScene.name = name
            save()
        }
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
){

}