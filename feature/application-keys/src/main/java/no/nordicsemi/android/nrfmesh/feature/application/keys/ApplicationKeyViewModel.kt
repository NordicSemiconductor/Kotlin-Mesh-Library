package no.nordicsemi.android.nrfmesh.feature.application.keys

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
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeyDestination
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import javax.inject.Inject

@HiltViewModel
internal class ApplicationKeyViewModel @Inject internal constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CoreDataRepository
) : ViewModel() {
    private val appKeyIndexArg: KeyIndex =
        checkNotNull(savedStateHandle[ApplicationKeyDestination.appKeyIndexArg]) as KeyIndex

    private val _uiState = MutableStateFlow(ApplicationKeyScreenUiState(KeyState.Loading))
    val uiState: StateFlow<ApplicationKeyScreenUiState> = _uiState.asStateFlow()

    init {
        repository.network.onEach { meshNetwork ->
            _uiState.update { state ->
                val key = meshNetwork.applicationKey(appKeyIndexArg)
                when (val keyState = state.keyState) {
                    is KeyState.Loading -> ApplicationKeyScreenUiState(
                        keyState = KeyState.Success(
                            key = key,
                            networkKeys = meshNetwork.networkKeys.toList()
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
     * Invoked when the name of the application key is changed.
     *
     * @param name New application key name.
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
     * Invoked when the application key is changed.
     *
     * @param key New application key.
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
     * Invoked when the bound network key is changed.
     *
     * @param key New network key to bind to
     */
    internal fun onBoundNetworkKeyChanged(key: NetworkKey) {
        _uiState.update { state ->
            val keyState = state.keyState as KeyState.Success
            keyState.key.boundNetKeyIndex = key.index
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
    data class Success(
        val key: ApplicationKey,
        val networkKeys: List<NetworkKey>
    ) : KeyState

    data class Error(val throwable: Throwable) : KeyState
    data object Loading : KeyState
}

data class ApplicationKeyScreenUiState internal constructor(
    val keyState: KeyState = KeyState.Loading
)