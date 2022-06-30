package no.nordicsemi.android.nrfmesh.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val storageRepository: DataStoreRepository
) : ViewModel() {
    var isNetworkLoaded by mutableStateOf(false)

    fun loadNetwork() {
        viewModelScope.launch {
            isNetworkLoaded = storageRepository.loadNetwork()
        }
    }
}