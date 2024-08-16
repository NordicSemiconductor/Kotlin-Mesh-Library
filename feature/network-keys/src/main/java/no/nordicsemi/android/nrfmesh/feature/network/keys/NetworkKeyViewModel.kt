package no.nordicsemi.android.nrfmesh.feature.network.keys

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import javax.inject.Inject

@HiltViewModel
internal class NetworkKeyViewModel @Inject internal constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CoreDataRepository
) : ViewModel() {
    private val keyIndex: KeyIndex = checkNotNull(savedStateHandle[MeshNavigationDestination.ARG])
        .toString()
        .toUShort()

    private val _uiState = MutableStateFlow(NetworkKeyScreenUiState(KeyState.Loading))
    val uiState: StateFlow<NetworkKeyScreenUiState> = _uiState.asStateFlow()

    init {
        repository.network.onEach { meshNetwork ->
            _uiState.update { state ->
                val key = meshNetwork.networkKey(keyIndex)
                when (val keyState = state.keyState) {
                    is KeyState.Loading -> NetworkKeyScreenUiState(
                        keyState = KeyState.Success(
                            key = key,
                        )
                    )

                    is KeyState.Success -> state.copy(
                        keyState = keyState.copy(
                            key = key
                        )
                    )

                    else -> state
                }
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Invoked when the name of the network key is changed.
     *
     * @param name New network key name.
     */
    internal fun onNameChanged(name: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                val keyState = state.keyState as KeyState.Success
                val key = keyState.key.apply {
                    this.name = name
                }
                state.copy(keyState = keyState.copy(key = key))
            }
        }
        save()
    }

    /**
     * Invoked when the network key is changed.
     *
     * @param key New network key.
     */
    internal fun onKeyChanged(key: ByteArray) {
        _uiState.update { state ->
            val keyState = state.keyState as KeyState.Success
            keyState.key.apply { setKey(key) }
            state.copy(keyState = keyState)
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

sealed interface KeyState {
    data class Success(val key: NetworkKey) : KeyState
    data class Error(val throwable: Throwable) : KeyState
    data object Loading : KeyState
}

data class NetworkKeyScreenUiState internal constructor(
    val keyState: KeyState = KeyState.Loading
)