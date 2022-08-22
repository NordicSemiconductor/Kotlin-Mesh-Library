package no.nordicsemi.android.nrfmesh.feature.network.keys

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeyDestination
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.crypto.Utils.decodeHex
import javax.inject.Inject

@HiltViewModel
class NetworkKeyViewModel @Inject internal constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: DataStoreRepository
) : ViewModel() {
    private val netKeyIndexArg: String =
        checkNotNull(savedStateHandle[NetworkKeyDestination.netKeyIndexArg])

    val uiState: StateFlow<NetworkKeyScreenUiState> =
        repository.network.map { network ->
            val state = runCatching {
                NetworkKeyState.Success(
                    networkKey = network.networkKey(netKeyIndexArg.toUShort())
                )
            }.getOrElse {
                NetworkKeyState.Error(throwable = it)
            }
            NetworkKeyScreenUiState(networkKeyState = state)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            NetworkKeyScreenUiState(NetworkKeyState.Loading)
        )

    /**
     * Invoked when the network key name is changed.
     *
     * @param name New network key name.
     */
    internal fun onNameChanged(name: String) {
        uiState.value.networkKeyState.run {
            if (this is NetworkKeyState.Success) {
                networkKey.name = name
                save()
            }
        }
    }

    /**
     * Invoked when the network key is changed.
     *
     * @param key New network key.
     */
    internal fun onKeyChanged(key: String) {
        uiState.value.networkKeyState.run {
            if (this is NetworkKeyState.Success) {
                networkKey.setKey(key = key.decodeHex())
                save()
            }
        }
    }

    private fun save() {
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