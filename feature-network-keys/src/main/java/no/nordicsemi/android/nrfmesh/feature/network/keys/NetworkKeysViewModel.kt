package no.nordicsemi.android.nrfmesh.feature.network.keys

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import javax.inject.Inject

@HiltViewModel
class NetworkKeysViewModel @Inject internal constructor(
    private val repository: DataStoreRepository
) : ViewModel() {

    var uiState by mutableStateOf(NetworkKeysScreenUiState())
        private set

    private val _v: MutableStateFlow<NetworkKeysScreenUiState> =
        MutableStateFlow(NetworkKeysScreenUiState())
    val v: StateFlow<NetworkKeysScreenUiState>
        get() = _v.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            NetworkKeysScreenUiState()
        )

    init {
        viewModelScope.launch {
            repository.network.collectLatest { network ->
                uiState = uiState.copy(keys = network.networkKeys)
            }
        }
    }
}

sealed interface NetworkKeysScreenState {
    object Success : NetworkKeysScreenState
    data class Error(val throwable: Throwable) : NetworkKeysScreenState
    object Unknown : NetworkKeysScreenState
}

data class NetworkKeysScreenUiState internal constructor(val keys: List<NetworkKey> = listOf())