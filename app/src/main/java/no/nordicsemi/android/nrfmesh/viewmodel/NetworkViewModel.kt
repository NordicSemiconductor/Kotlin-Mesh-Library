package no.nordicsemi.android.nrfmesh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import javax.inject.Inject

@HiltViewModel
class NetworkViewModel @Inject constructor(
    private val repository: CoreDataRepository
) : ViewModel() {
    private lateinit var meshNetwork: MeshNetwork

    init {
        loadNetwork()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            repository.disconnect()
        }
    }

    /**
     * Loads the network
     */
    private fun loadNetwork() {
        viewModelScope.launch {
            meshNetwork = repository.load()
            repository.startAutomaticConnectivity(meshNetwork)
        }
    }
}