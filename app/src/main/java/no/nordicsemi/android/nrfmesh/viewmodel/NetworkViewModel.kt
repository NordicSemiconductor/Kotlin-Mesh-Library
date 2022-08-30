package no.nordicsemi.android.nrfmesh.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import javax.inject.Inject

@HiltViewModel
class NetworkViewModel @Inject constructor(
    private val repository: DataStoreRepository
) : ViewModel() {

    var isNetworkLoaded by mutableStateOf(false)

    init {
        loadNetwork()
    }

    /**
     * Loads the network
     */
    private fun loadNetwork() {
        viewModelScope.launch {
            isNetworkLoaded = repository.load()
            repository.network.collect{
                Log.d("AAAA", "NetworkViewModel: $it")
            }
        }
    }
}