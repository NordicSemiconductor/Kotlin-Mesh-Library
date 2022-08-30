package no.nordicsemi.android.nrfmesh.feature.network.keys

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
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import javax.inject.Inject

@HiltViewModel
class NetworkKeysViewModel @Inject internal constructor(
    private val repository: DataStoreRepository
) : ViewModel() {
    private lateinit var network: MeshNetwork
    private var keysToBeRemoved = mutableListOf<NetworkKey>()

    val uiState: StateFlow<NetworkKeysScreenUiState> =
        repository.network.map { network ->
            this@NetworkKeysViewModel.network = network
            val keys = mutableListOf<NetworkKey>()
            keys.addAll(network.networkKeys)
            NetworkKeysScreenUiState(keys = keys)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            NetworkKeysScreenUiState()
        )

    /**
     * Adds a network key to the network.
     */
    internal fun addNetworkKey(): NetworkKey {
        // Let's delete any keys that are queued for deletion before adding a new.
        removeSelectedKeys()
        return network.add(name = "nRF Network Key")
    }

    /**
     * Saves the network.
     */
    private fun save() {
        viewModelScope.launch {
            repository.save()
        }
    }

    /**
     * Invoked when a key is swiped to be deleted. The given key is added to a list of keys that
     * is to be deleted.
     *
     * @param key Network key to be deleted.
     */
    fun onSwiped(key: NetworkKey) {
        if (!keysToBeRemoved.contains(key))
            keysToBeRemoved.add(key)
    }

    /**
     * Invoked when a key is swiped to be deleted is undone. When invoked the given key is removed
     * from the list of keys to be deleted.
     *
     * @param key Network key to be reverted.
     */
    fun onUndoSwipe(key: NetworkKey) {
        keysToBeRemoved.remove(key)
    }

    /**
     * Starts a coroutines that removes the keys from a network,
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
        network.networkKeys.filter {
            it in keysToBeRemoved
        }.forEach {
            network.remove(it)
        }
        keysToBeRemoved.clear()
        save()
    }
}

data class NetworkKeysScreenUiState internal constructor(
    val keys: List<NetworkKey> = listOf()
)
