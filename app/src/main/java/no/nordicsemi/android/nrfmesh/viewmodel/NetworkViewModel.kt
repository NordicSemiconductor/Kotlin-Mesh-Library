package no.nordicsemi.android.nrfmesh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.core.data.storage.MeshSecurePropertiesStorage
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi

@HiltViewModel
class NetworkViewModel @Inject constructor(
    private val repository: CoreDataRepository,
    private val storage: MeshSecurePropertiesStorage,
) : ViewModel() {
    private var meshNetwork: MeshNetwork? = null
    private val _uiState = MutableStateFlow(NetworkScreenUiState())
    internal val uiState: StateFlow<NetworkScreenUiState> = _uiState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NetworkScreenUiState()
        )

    init {
        loadNetwork()
        observeNetworkChanges()
    }

    private fun observeNetworkChanges() {
        // Observes the mesh network for any changes i.e. network reset etc.
        repository.network
            .onEach { network ->
                _uiState.update { state ->
                    state.copy(
                        networkState = MeshNetworkState.Success(network = network),
                        counter = state.counter + 1
                    )
                }
                promptProvisionerSelection(network)
                meshNetwork = network
            }
            .launchIn(scope = viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            repository.disconnect()
        }
    }

    internal fun loadNetwork() {
        viewModelScope.launch {
            // Check if a network can be loaded and if not update the state
            if (!repository.load()) {
                _uiState.update { state ->
                    state.copy(networkState = MeshNetworkState.NoNetwork)
                }
            } else {
                repository.startAutomaticConnectivity(meshNetwork)
            }
        }
    }

    /**
     * Selects the given provisioner.
     *
     * @param provisioner Provisioner to be selected.
     */
    @OptIn(ExperimentalUuidApi::class)
    internal fun onProvisionerSelected(provisioner: Provisioner) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(shouldSelectProvisioner = false)
            meshNetwork?.let { meshNetwork ->
                meshNetwork.move(provisioner = provisioner, to = 0)
                storage.storeLocalProvisioner(
                    uuid = meshNetwork.uuid,
                    localProvisionerUuid = provisioner.uuid
                )
                repository.save()
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun promptProvisionerSelection(meshNetwork: MeshNetwork) {
        if (!meshNetwork.restoreLocalProvisioner(storage = storage)) {
            meshNetwork
                .takeIf { it.provisioners.size > 1 }
                ?.let { _uiState.value = _uiState.value.copy(shouldSelectProvisioner = true) }
                ?: run {
                    storage.storeLocalProvisioner(
                        uuid = meshNetwork.uuid,
                        localProvisionerUuid = meshNetwork.provisioners.first().uuid
                    )
                }
        }
    }

    internal fun resetNetwork() {
        viewModelScope.launch { repository.resetNetwork() }
    }

    fun resetMeshNetworkUiState() {
        _uiState.update {
            it.copy(networkState = MeshNetworkState.Loading)
        }
    }
}

internal sealed interface MeshNetworkState {
    data object Loading : MeshNetworkState
    data class Success(val network: MeshNetwork) : MeshNetworkState
    data object NoNetwork : MeshNetworkState
}

internal data class NetworkScreenUiState(
    val networkState: MeshNetworkState = MeshNetworkState.Loading,
    val shouldSelectProvisioner: Boolean = false,
    val counter: Int = 0,
)