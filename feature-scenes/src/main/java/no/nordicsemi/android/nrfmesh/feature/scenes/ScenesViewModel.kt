package no.nordicsemi.android.nrfmesh.feature.scenes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Scene
import javax.inject.Inject

@HiltViewModel
internal class ScenesViewModel @Inject internal constructor(
    savedStateHandle: SavedStateHandle,
    navigator: Navigator,
    private val repository: CoreDataRepository
) : SimpleNavigationViewModel(navigator, savedStateHandle) {
    private val _uiState = MutableStateFlow(ScenesScreenUiState(listOf()))
    val uiState: StateFlow<ScenesScreenUiState> = _uiState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ScenesScreenUiState()
    )

    private lateinit var network: MeshNetwork

    init {
        viewModelScope.launch {
            repository.network.collect { network ->
                this@ScenesViewModel.network = network
                val scenes = network.scenes.toList()
                _uiState.update { state ->
                    state.copy(
                        scenes = scenes,
                        hasProvisioners = network.provisioners.isNotEmpty(),
                        scenesToBeRemoved = scenes.filter { it in state.scenesToBeRemoved }
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        removeScenes()
    }

    /**
     * Adds a scene to the network.
     */
    internal fun addScene() = network.nextAvailableScene()?.let {
        network.add(name = "nRF Scene", number = it)
    }

    /**
     * Invoked when a scene is swiped to be deleted. The given scene is added to a list of scenes
     * that is to be deleted.
     *
     * @param scene Scene to be deleted.
     */
    internal fun onSwiped(scene: Scene) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(scenesToBeRemoved = it.scenes + scene)
            }
        }
    }

    /**
     * Invoked when a scene is swiped to be deleted is undone. When invoked the given scene
     * is removed from the list of scenes to be deleted.
     *
     * @param scene Scene to be reverted.
     */
    internal fun onUndoSwipe(scene: Scene) {
        _uiState.update {
            it.copy(scenesToBeRemoved = it.scenesToBeRemoved - scene)
        }
    }

    /**
     * Remove a given scene from the network.
     *
     * @param scene Scene to be removed.
     */
    internal fun remove(scene: Scene) {
        _uiState.update {
            it.copy(scenesToBeRemoved = it.scenesToBeRemoved - scene)
        }
        network.remove(scene)
        save()
    }

    /**
     * Removes the scene from a network.
     */
    private fun removeScenes() {
        _uiState.value.scenesToBeRemoved.forEach {
            network.remove(it)
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

data class ScenesScreenUiState internal constructor(
    val scenes: List<Scene> = listOf(),
    val hasProvisioners: Boolean = false,
    val scenesToBeRemoved: List<Scene> = listOf()
)
