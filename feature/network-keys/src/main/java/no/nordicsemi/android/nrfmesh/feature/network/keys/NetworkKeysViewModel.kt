package no.nordicsemi.android.nrfmesh.feature.network.keys

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.core.data.models.NetworkKeyData
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import javax.inject.Inject

@HiltViewModel
class NetworkKeysViewModel @Inject internal constructor(
    private val repository: CoreDataRepository,
) : ViewModel() {

    private lateinit var network: MeshNetwork
    private var selectedKeyIndex: KeyIndex? = null
    private val _uiState = MutableStateFlow(NetworkKeysScreenUiState(listOf()))
    val uiState: StateFlow<NetworkKeysScreenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.network.collect { network ->
                this@NetworkKeysViewModel.network = network
                val state = _uiState.value
                val keys = network.networkKeys.map { NetworkKeyData(it) }
                _uiState.value = state.copy(
                    keys = keys,
                    keysToBeRemoved = keys.filter { it in state.keysToBeRemoved }
                )
            }
        }
    }

    override fun onCleared() {
        removeAllKeys()
        super.onCleared()
    }

    /**
     * Adds a network key to the network.
     */
    internal fun addNetworkKey() = repository.addNetworkKey()

    /**
     * Invoked when a key is swiped to be deleted. The given key is added to a list of keys that
     * is to be deleted.
     *
     * @param key Network key to be deleted.
     */

    fun onSwiped(key: NetworkKeyData) {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.value = state.copy(
                keys = state.keys - key,
                keysToBeRemoved = state.keysToBeRemoved + key
            )
        }
    }

    /**
     * Invoked when a key is swiped to be deleted is undone. When invoked the given key is removed
     * from the list of keys to be deleted.
     *
     * @param key Network key to be reverted.
     */
    fun onUndoSwipe(key: NetworkKeyData) {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.value = state.copy(
                keys = (state.keys + key)
                    .sortedBy { it.index },
                keysToBeRemoved = state.keysToBeRemoved - key
            )
        }
    }

    /**
     * Remove a given key from the network.
     *
     * @param key Key to be removed.
     */
    internal fun remove(key: NetworkKeyData) {
        viewModelScope.launch {
            val state = _uiState.value
            runCatching {
                network.run {
                    networkKey(index = key.index)
                        ?.let { remove(key = it) }
                    save()
                }
            }
            _uiState.value = state.copy(keysToBeRemoved = state.keysToBeRemoved - key)
        }
    }

    /**
     * Removes all keys that are queued for deletion.
     */
    private fun removeAllKeys() {
        runCatching {
            _uiState.value.keysToBeRemoved.forEach { keyData ->
                network.run {
                    networkKey(index = keyData.index)
                        ?.let { remove(key = it) }
                }
            }
            save()
        }
    }

    /**
     * Saves the network.
     */
    private fun save() {
        viewModelScope.launch {
            repository.save()
        }
    }

    internal fun selectKeyIndex(keyIndex: KeyIndex) {
        selectedKeyIndex = keyIndex
    }

    internal fun isCurrentlySelectedKey(keyIndex: KeyIndex): Boolean =
        keyIndex == selectedKeyIndex
}

@ConsistentCopyVisibility
data class NetworkKeysScreenUiState internal constructor(
    val keys: List<NetworkKeyData> = listOf(),
    val keysToBeRemoved: List<NetworkKeyData> = listOf(),
)
