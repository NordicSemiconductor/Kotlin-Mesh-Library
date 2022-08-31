package no.nordicsemi.android.feature.scenes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
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
    private lateinit var network: MeshNetwork
    private var scenesToBeRemoved = mutableListOf<Scene>()

    val uiState: StateFlow<ScenesScreenUiState> =
        repository.network.map { network ->
            this@ScenesViewModel.network = network
            ScenesScreenUiState(
                scenes = mutableListOf<Scene>().apply {
                    addAll(network.scenes)
                }.toList()
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ScenesScreenUiState()
        )

    /**
     * Adds a scene to the network.
     */
    internal fun addScene(): Scene? {
        // Let's delete any keys that are queued for deletion before adding a new.
        removeSelectedScenes()
        return network.nextAvailableScene(network.provisioners[1])?.let {
            network.add(
                name = "nRF Scene",
                number = it
            )
        }
    }

    private fun save() {
        viewModelScope.launch {
            repository.save()
        }
    }

    /**
     * Invoked when a scene is swiped to be deleted. The given scene is added to a list of scenes
     * that is to be deleted.
     *
     * @param scene Scene to be deleted.
     */
    fun onSwiped(scene: Scene) {
        if (!scenesToBeRemoved.contains(scene))
            scenesToBeRemoved.add(scene)
    }

    /**
     * Invoked when a scene is swiped to be deleted is undone. When invoked the given scene
     * is removed from the list of scenes to be deleted.
     *
     * @param scene Scene to be reverted.
     */
    fun onUndoSwipe(scene: Scene) {
        scenesToBeRemoved.remove(scene)
    }

    /**
     * Starts a coroutines that removes the scene from a network.
     */
    fun removeScenes() {
        if (scenesToBeRemoved.isNotEmpty()) {
            removeSelectedScenes()
            save()
        }
    }

    /**
     * Removes the selected scemes from a given network.
     */
    private fun removeSelectedScenes() {
        network.scenes.filter {
            it in scenesToBeRemoved
        }.forEach {
            network.remove(it)
        }
        scenesToBeRemoved.clear()
        save()
    }
}

data class ScenesScreenUiState internal constructor(
    val scenes: List<Scene> = listOf()
)
