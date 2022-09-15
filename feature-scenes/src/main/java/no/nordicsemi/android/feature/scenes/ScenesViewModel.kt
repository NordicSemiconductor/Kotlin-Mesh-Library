package no.nordicsemi.android.feature.scenes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Scene
import javax.inject.Inject

@HiltViewModel
class ScenesViewModel @Inject internal constructor(
    private val repository: DataStoreRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScenesScreenUiState(listOf()))
    val uiState: StateFlow<ScenesScreenUiState> = _uiState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ScenesScreenUiState()
    )

    private lateinit var network: MeshNetwork
    private var scenesToBeRemoved = mutableListOf<Scene>()

    init {
        viewModelScope.launch {
            repository.network.collect { network ->
                Log.d("AAAA", "Update received")
                this@ScenesViewModel.network = network
                _uiState.value = ScenesScreenUiState(scenes = filterScenes())
            }
        }
    }


    /**
     * Adds a scene to the network.
     */
    internal fun addScene(): Scene? {
        removeScenes()
        return network.nextAvailableScene(network.provisioners[1])?.let {
            network.add(name = "nRF Scene", number = it)
        }
    }

    /**
     * Invoked when a scene is swiped to be deleted. The given scene is added to a list of scenes
     * that is to be deleted.
     *
     * @param scene Scene to be deleted.
     */
    internal fun onSwiped(scene: Scene) {
        if (!scenesToBeRemoved.contains(scene))
            scenesToBeRemoved.add(scene)
        // _uiState.value = ScenesScreenUiState(filterScenes())
    }

    /**
     * Invoked when a scene is swiped to be deleted is undone. When invoked the given scene
     * is removed from the list of scenes to be deleted.
     *
     * @param scene Scene to be reverted.
     */
    internal fun onUndoSwipe(scene: Scene) {
        scenesToBeRemoved.remove(scene)
        // _uiState.value = ScenesScreenUiState(filterScenes())
    }

    internal fun remove(scene: Scene) {
        network.apply {
            scenes.find { it == scene }?.let {
                remove(it)
            }
        }
        scenesToBeRemoved.remove(scene)
    }

    private fun remove() {
        network.scenes.filter {
            it in scenesToBeRemoved
        }.forEach {
            network.remove(it)
        }
        scenesToBeRemoved.clear()
    }

    /**
     * Starts a coroutines that removes the scene from a network.
     */
    internal fun removeScenes() {
        remove()
        save()
    }

    private fun save() {
        viewModelScope.launch { repository.save() }
    }

    private fun filterScenes() = network.scenes.filter {
        it !in scenesToBeRemoved
    }
}

data class ScenesScreenUiState internal constructor(val scenes: List<Scene> = listOf())
