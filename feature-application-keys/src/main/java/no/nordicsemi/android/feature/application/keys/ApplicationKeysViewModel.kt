package no.nordicsemi.android.feature.application.keys

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import javax.inject.Inject

@HiltViewModel
class ApplicationKeysViewModel @Inject internal constructor(
    private val repository: DataStoreRepository
) : ViewModel() {
    private lateinit var network: MeshNetwork
    private var keysToBeRemoved = mutableListOf<ApplicationKey>()

    val uiState: StateFlow<ApplicationKeysScreenUiState> =
        repository.network.map { network ->
            this@ApplicationKeysViewModel.network = network
            val keys = mutableListOf<ApplicationKey>()
            keys.addAll(network.applicationKeys)
            ApplicationKeysScreenUiState(keys = keys)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ApplicationKeysScreenUiState()
        )

    /**
     * Adds an application key to the network.
     */
    internal fun addApplicationKey(): ApplicationKey {
        // Let's delete any keys that are queued for deletion before adding a new.
        removeSelectedKeys()
        return network.add(
            name = "nRF Application Key",
            boundNetworkKey = network.networkKeys.first()
        )
    }

    private fun save() {
        viewModelScope.launch {
            repository.save()
        }
    }

    /**
     * Invoked when a key is swiped to be deleted. The given key is added to a list of keys that
     * is to be deleted.
     *
     * @param key Application key to be deleted.
     */
    fun onSwiped(key: ApplicationKey) {
        if (!keysToBeRemoved.contains(key))
            keysToBeRemoved.add(key)
    }

    /**
     * Invoked when a key is swiped to be deleted is undone. When invoked the given key is removed
     * from the list of keys to be deleted.
     *
     * @param key Application key to be reverted.
     */
    fun onUndoSwipe(key: ApplicationKey) {
        keysToBeRemoved.remove(key)
    }

    /**
     * Starts a coroutines that removes the keys from a network.
     */
    fun removeKeys() {
        if (keysToBeRemoved.isNotEmpty()) {
            removeSelectedKeys()
            save()
        }
    }

    /**
     * Removes the selected keys from a given network.
     */
    private fun removeSelectedKeys() {
        network.applicationKeys.filter {
            it in keysToBeRemoved
        }.forEach {
            network.remove(it)
        }
        keysToBeRemoved.clear()
        save()
    }
}

data class ApplicationKeysScreenUiState internal constructor(
    val keys: List<ApplicationKey> = listOf()
)
