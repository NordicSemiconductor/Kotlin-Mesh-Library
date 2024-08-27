package no.nordicsemi.android.nrfmesh.feature.scenes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Scene
import javax.inject.Inject

@HiltViewModel
internal class ScenesViewModel @Inject internal constructor(
    private val repository: CoreDataRepository
) : ViewModel() {

    private lateinit var network: MeshNetwork

    private val _uiState = MutableStateFlow(ScenesScreenUiState())
    val uiState: StateFlow<ScenesScreenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.network.collect { network ->
                this@ScenesViewModel.network = network
                _uiState.update { state ->
                    val scenes = network.scenes.toList()
                    state.copy(
                        scenes = scenes,
                        scenesToBeRemoved = scenes.filter { it in state.scenesToBeRemoved }
                    )
                }
            }
        }
    }

    override fun onCleared() {
        removeScenes()
        super.onCleared()
    }

    /**
     * Adds a scene to the network.
     */
    internal fun addScene() = network.nextAvailableScene()?.let {
        network.add(name = "nRF Scene", number = it).also {
            save()
        }
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
                it.copy(scenesToBeRemoved = it.scenesToBeRemoved + scene)
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
    val scenesToBeRemoved: List<Scene> = listOf()
)
