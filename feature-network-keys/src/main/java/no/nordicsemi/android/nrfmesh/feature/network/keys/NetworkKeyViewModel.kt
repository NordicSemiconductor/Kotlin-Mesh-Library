package no.nordicsemi.android.nrfmesh.feature.network.keys

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeyDestination
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import javax.inject.Inject

@HiltViewModel
class NetworkKeyViewModel @Inject internal constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: DataStoreRepository
) : ViewModel() {
    private lateinit var networkKey: NetworkKey
    private val netKeyIndexArg: String =
        checkNotNull(savedStateHandle[NetworkKeyDestination.netKeyIndexArg])

    val uiState: StateFlow<NetworkKeyScreenUiState> = repository.network.map { network ->
        this@NetworkKeyViewModel.networkKey = network.networkKey(netKeyIndexArg.toUShort())
        NetworkKeyScreenUiState(
            networkKeyState = NetworkKeyState.Success(networkKey = networkKey)
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        NetworkKeyScreenUiState(NetworkKeyState.Loading)
    )

    /**
     * Invoked when the name of the network key is changed.
     *
     * @param name New network key name.
     */
    internal fun onNameChanged(name: String) {
        if (networkKey.name != name) {
            networkKey.name = name
            save()
        }
    }

    /**
     * Invoked when the network key is changed.
     *
     * @param key New network key.
     */
    internal fun onKeyChanged(key: ByteArray) {
        if (!networkKey.key.contentEquals(key)) {
            networkKey.setKey(key = key)
            save()
        }
    }

    /**
     * Saves the network.
     */
    internal fun save() {
        viewModelScope.launch { repository.save() }
    }
}

sealed interface NetworkKeyState {
    data class Success(val networkKey: NetworkKey) : NetworkKeyState
    data class Error(val throwable: Throwable) : NetworkKeyState
    object Loading : NetworkKeyState
}

@Suppress("ArrayInDataClass")
data class NetworkKeyScreenUiState internal constructor(
    val networkKeyState: NetworkKeyState = NetworkKeyState.Loading
)