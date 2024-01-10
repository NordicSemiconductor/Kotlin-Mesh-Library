package no.nordicsemi.android.nrfmesh.feature.network.keys

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.DestinationId
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import javax.inject.Inject

@HiltViewModel
internal class NetworkKeysViewModel @Inject internal constructor(
    savedStateHandle: SavedStateHandle,
    private val navigator: Navigator,
    private val repository: CoreDataRepository
) : SimpleNavigationViewModel(navigator, savedStateHandle) {

    private lateinit var network: MeshNetwork

    private val _uiState = MutableStateFlow(NetworkKeysScreenUiState(listOf()))
    val uiState: StateFlow<NetworkKeysScreenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.network.collect { network ->
                this@NetworkKeysViewModel.network = network
                _uiState.update { state ->
                    val keys = network.networkKeys.toList()
                    state.copy(
                        keys = keys,
                        keysToBeRemoved = keys.filter { it in state.keysToBeRemoved }
                    )
                }
            }
        }
    }

    override fun onCleared() {
        removeAllKeys()
        super.onCleared()
    }

    internal fun navigate(destinationId: DestinationId<Int, Unit>, keyIndex: Int) {
        navigator.navigateTo(destinationId, keyIndex)
    }

    /**
     * Adds a network key to the network.
     */
    internal fun addNetworkKey(): NetworkKey = network.add(name = "nRF Network Key")

    /**
     * Invoked when a key is swiped to be deleted. The given key is added to a list of keys that
     * is to be deleted.
     *
     * @param key Network key to be deleted.
     */

    fun onSwiped(key: NetworkKey) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(keysToBeRemoved = it.keysToBeRemoved + key)
            }
        }
    }

    /**
     * Invoked when a key is swiped to be deleted is undone. When invoked the given key is removed
     * from the list of keys to be deleted.
     *
     * @param key Network key to be reverted.
     */

    fun onUndoSwipe(key: NetworkKey) {
        _uiState.update {
            it.copy(keysToBeRemoved = it.keysToBeRemoved - key)
        }
    }

    /**
     * Remove a given key from the network.
     *
     * @param key Key to be removed.
     */
    internal fun remove(key: NetworkKey) {
        _uiState.update {
            it.copy(keysToBeRemoved = it.keysToBeRemoved - key)
        }
        network.remove(key)
        save()
    }

    /**
     * Removes all keys that are queued for deletion.
     */
    private fun removeAllKeys() {
        _uiState.value.keysToBeRemoved.forEach {
            network.remove(it)
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
}

data class NetworkKeysScreenUiState internal constructor(
    val keys: List<NetworkKey> = listOf(),
    val keysToBeRemoved: List<NetworkKey> = listOf()
)
