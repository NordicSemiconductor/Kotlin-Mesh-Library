package no.nordicsemi.android.nrfmesh.feature.network.keys

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
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import javax.inject.Inject

@HiltViewModel
internal class NetworkKeysViewModel @Inject internal constructor(
    private val repository: DataStoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NetworkKeysScreenUiState(listOf()))
    val uiState: StateFlow<NetworkKeysScreenUiState> = _uiState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        NetworkKeysScreenUiState()
    )

    private lateinit var network: MeshNetwork
    private var keysToBeRemoved = mutableListOf<NetworkKey>()

    init {
        viewModelScope.launch {
            repository.network.collect { network ->
                this@NetworkKeysViewModel.network = network
                _uiState.value = NetworkKeysScreenUiState(keys = filterKeysToBeRemoved())
            }
        }
    }

    /**
     * Adds a network key to the network.
     */
    internal fun addNetworkKey(): NetworkKey {
        // Let's delete any keys that are queued for deletion before adding a new.
        removeKeys()
        return network.add(name = "nRF Network Key")
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
        if (keysToBeRemoved.size == network.networkKeys.size)
            _uiState.value = NetworkKeysScreenUiState(keys = filterKeysToBeRemoved())
    }

    /**
     * Invoked when a key is swiped to be deleted is undone. When invoked the given key is removed
     * from the list of keys to be deleted.
     *
     * @param key Network key to be reverted.
     */
    fun onUndoSwipe(key: NetworkKey) {
        keysToBeRemoved.remove(key)
        if (keysToBeRemoved.isEmpty())
            _uiState.value = NetworkKeysScreenUiState(keys = filterKeysToBeRemoved())
    }

    /**
     * Remove a given key from the network.
     *
     * @param key Key to be removed.
     */
    internal fun remove(key: NetworkKey) {
        network.apply {
            networkKeys.find { it == key }?.let {
                remove(it)
            }
        }
        keysToBeRemoved.remove(key)
    }

    /**
     * Removes the keys from a network.
     */
    internal fun removeKeys() {
        remove()
        save()
    }

    /**
     * Removes the keys from the network.
     */
    private fun remove() {
        network.networkKeys.filter {
            it in keysToBeRemoved
        }.forEach {
            network.remove(it)
        }
        keysToBeRemoved.clear()
    }

    /**
     * Saves the network.
     */
    private fun save() {
        viewModelScope.launch {
            repository.save()
        }
    }

    private fun filterKeysToBeRemoved() = network.networkKeys.filter {
        it !in keysToBeRemoved
    }
}

data class NetworkKeysScreenUiState internal constructor(
    val keys: List<NetworkKey> = listOf()
)
