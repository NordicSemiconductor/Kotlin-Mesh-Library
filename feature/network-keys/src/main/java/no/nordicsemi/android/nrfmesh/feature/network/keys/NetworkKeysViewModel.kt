package no.nordicsemi.android.nrfmesh.feature.network.keys

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.core.data.models.NetworkKeyData
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork

@HiltViewModel(assistedFactory = NetworkKeysViewModel.Factory::class)
class NetworkKeysViewModel @AssistedInject internal constructor(
    private val repository: CoreDataRepository,
) : ViewModel() {

    private lateinit var network: MeshNetwork
    private val _uiState = MutableStateFlow(NetworkKeysScreenUiState())
    val uiState: StateFlow<NetworkKeysScreenUiState> = _uiState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NetworkKeysScreenUiState()
        )

    init {
        observeNetwork()
    }

    override fun onCleared() {
        removeKeys()
        super.onCleared()
    }

    private fun observeNetwork() {
        repository.network.onEach { network ->
            this.network = network
            _uiState.update { state ->
                state.copy(
                    keys = network.networkKeys
                        .map { NetworkKeyData(key = it) }
                        // Filter out the keys that are marked for deletion.
                        .filter { it !in state.keysToBeRemoved },
                )
            }
        }.launchIn(scope = viewModelScope)
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
    internal fun onSwiped(key: NetworkKeyData) {
        _uiState.update { state ->
            state.copy(keysToBeRemoved = state.keysToBeRemoved + key)
        }
    }

    /**
     * Invoked when a key is swiped to be deleted is undone. When invoked the given key is removed
     * from the list of keys to be deleted.
     *
     * @param key Network key to be reverted.
     */
    internal fun onUndoSwipe(key: NetworkKeyData) {
        _uiState.update { state ->
            state.copy(keysToBeRemoved = state.keysToBeRemoved - key)
        }
    }

    /**
     * Remove a given key from the network.
     *
     * @param key Key to be removed.
     */
    internal fun remove(key: NetworkKeyData) {
        _uiState.update { state ->
            state.copy(
                keys = state.keys - key,
                keysToBeRemoved = state.keysToBeRemoved - key
            )
        }
        network.removeNetworkKeyWithIndex(index = key.index)
        // In addition lets remove the keys queued for deletion as well.
        removeKeys()
    }

    /**
     * Removes all keys that are queued for deletion.
     */
    private fun removeKeys() {
        runCatching {
            _uiState.value.keysToBeRemoved.forEach { keyData ->
                network.removeNetworkKeyWithIndex(index = keyData.index)
            }
        }
        save()
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
        _uiState.update { state ->
            state.copy(selectedKeyIndex = keyIndex)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(): NetworkKeysViewModel
    }
}

@ConsistentCopyVisibility
data class NetworkKeysScreenUiState internal constructor(
    val keys: List<NetworkKeyData> = listOf(),
    val keysToBeRemoved: List<NetworkKeyData> = listOf(),
    val selectedKeyIndex: KeyIndex? = null,
)
