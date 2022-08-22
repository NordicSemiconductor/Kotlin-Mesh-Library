package no.nordicsemi.android.nrfmesh.feature.network.keys

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import javax.inject.Inject

@HiltViewModel
class NetworkKeysViewModel @Inject internal constructor(
    repository: DataStoreRepository
) : ViewModel() {

    val uiState: StateFlow<NetworkKeysScreenUiState> = repository.network.map { network ->
        val keys = network.networkKeys
        NetworkKeysScreenUiState(keys = keys)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        NetworkKeysScreenUiState(keys = listOf())
    )
}

data class NetworkKeysScreenUiState internal constructor(val keys: List<NetworkKey> = listOf())