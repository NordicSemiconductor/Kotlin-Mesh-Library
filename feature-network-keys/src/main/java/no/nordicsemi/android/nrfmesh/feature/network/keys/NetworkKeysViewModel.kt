package no.nordicsemi.android.nrfmesh.feature.network.keys

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import javax.inject.Inject

@HiltViewModel
class NetworkKeysViewModel @Inject internal constructor(
    private val repository: DataStoreRepository
) : ViewModel() {
    val uiState: StateFlow<NetworkKeysScreenUiState> = repository.network.map { network ->
        NetworkKeysScreenUiState(network.networkKeys)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        NetworkKeysScreenUiState()
    )

    /**
     * Adds a network key to the network.
     */
    internal fun addNetworkKey() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.network.first().add(name = "nRF Network Key")
            repository.save()
        }
    }

    /**
     * Removes the given key from the network.
     *
     * @param key Network key.
     */
    fun removeKey(key: NetworkKey) {
        viewModelScope.launch {
            repository.network.first().remove(key)
            repository.save()
        }
    }
}

data class NetworkKeysScreenUiState internal constructor(val keys: List<NetworkKey> = listOf())
