package no.nordicsemi.android.nrfmesh.feature.network.keys.key

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
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
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.kotlin.data.HexString
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey

@HiltViewModel(assistedFactory = NetworkKeyViewModel.Factory::class)
class NetworkKeyViewModel @AssistedInject internal constructor(
    private val repository: CoreDataRepository,
    @Assisted index: HexString,
) : ViewModel() {
    private val keyIndex = index.toUShort(radix = 16)
    private lateinit var network: MeshNetwork
    private val _uiState = MutableStateFlow(NetworkKeyScreenUiState())
    internal val uiState: StateFlow<NetworkKeyScreenUiState> = _uiState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NetworkKeyScreenUiState()
        )

    init {
        observeNetwork()
    }

    private fun observeNetwork() {
        repository.network.onEach { network ->
            this.network = network
            val keyState = network.networkKey(index = keyIndex)?.let { key ->
                NetKeyState.Success(key = key)
            } ?: NetKeyState.Error(throwable = IllegalStateException("Network Key not found."))
            _uiState.update { state ->
                state.copy(keyState = keyState)
            }
        }.launchIn(scope = viewModelScope)
    }

    /**
     * Saves the network.
     */
    internal fun save() {
        repository.save()
    }

    @AssistedFactory
    interface Factory {
        fun create(index: HexString): NetworkKeyViewModel
    }
}

internal sealed interface NetKeyState {

    data object Loading : NetKeyState

    data class Success(val key: NetworkKey) : NetKeyState

    data class Error(val throwable: Throwable) : NetKeyState
}

@ConsistentCopyVisibility
internal data class NetworkKeyScreenUiState internal constructor(
    val keyState: NetKeyState = NetKeyState.Loading,
)
