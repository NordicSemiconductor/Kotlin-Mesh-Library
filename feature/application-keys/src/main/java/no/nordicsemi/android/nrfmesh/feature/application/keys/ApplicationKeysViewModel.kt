package no.nordicsemi.android.nrfmesh.feature.application.keys

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
import no.nordicsemi.android.nrfmesh.core.data.models.ApplicationKeyData
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import javax.inject.Inject

@HiltViewModel(assistedFactory = ApplicationKeysViewModel.Factory::class)
internal class ApplicationKeysViewModel @AssistedInject internal constructor(
    private val repository: CoreDataRepository,
) : ViewModel() {

    private lateinit var network: MeshNetwork

    private val _uiState = MutableStateFlow(ApplicationKeysScreenUiState())
    val uiState: StateFlow<ApplicationKeysScreenUiState> = _uiState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ApplicationKeysScreenUiState()
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
                    keys = network.applicationKeys
                        .map { ApplicationKeyData(key = it) }
                        // Filter out the keys that are marked for deletion.
                        .filter { it !in state.keysToBeRemoved },
                )
            }
        }.launchIn(scope = viewModelScope)
    }

    /**
     * Adds an application key to the network.
     */
    internal fun addApplicationKey(
        name: String = "Application Key ${network.nextAvailableApplicationKeyIndex}",
        boundNetworkKey: NetworkKey = network.networkKeys.first(),
    ) = repository.addApplicationKey(name = name, boundNetworkKey = boundNetworkKey)

    /**
     * Invoked when a key is swiped to be deleted. The given key is added to a list of keys to be
     * deleted.
     *
     * @param key Application key to be deleted.
     */
    fun onSwiped(key: ApplicationKeyData) {
        _uiState.update { state ->
            state.copy(keysToBeRemoved = state.keysToBeRemoved + key)
        }
    }

    /**
     * Invoked when a key is swiped to be deleted is undone. When invoked the given key is removed
     * from the list of keys to be deleted.
     *
     * @param key Application key to be reverted.
     */
    fun onUndoSwipe(key: ApplicationKeyData) {
        _uiState.update { state ->
            state.copy(keysToBeRemoved = state.keysToBeRemoved - key)
        }
    }

    /**
     * Remove a given application key from the network.
     *
     * @param key Key to be removed.
     */
    internal fun remove(key: ApplicationKeyData) {
        _uiState.update { state ->
            state.copy(
                keys = state.keys - key,
                keysToBeRemoved = state.keysToBeRemoved - key
            )
        }
        network.removeApplicationKeyWithIndex(index = key.index)
        // In addition lets remove the keys queued for deletion as well.
        removeKeys()
    }

    /**
     * Removes all keys that are queued for deletion.
     */
    private fun removeKeys() {
        runCatching {
            _uiState.value.keysToBeRemoved.forEach { keyData ->
                network.removeApplicationKeyWithIndex(index = keyData.index)
            }
        }
        save()
    }

    /**
     * Saves the network.
     */
    private fun save() {
        viewModelScope.launch { repository.save() }
    }


    internal fun selectKeyIndex(keyIndex: KeyIndex) {
        _uiState.update { state ->
            state.copy(selectedKeyIndex = keyIndex)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(): ApplicationKeysViewModel
    }
}

@ConsistentCopyVisibility
data class ApplicationKeysScreenUiState internal constructor(
    val keys: List<ApplicationKeyData> = listOf(),
    val keysToBeRemoved: List<ApplicationKeyData> = listOf(),
    val selectedKeyIndex: KeyIndex? = null,
)
