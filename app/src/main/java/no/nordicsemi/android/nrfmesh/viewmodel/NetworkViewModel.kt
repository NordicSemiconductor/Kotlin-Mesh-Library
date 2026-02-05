package no.nordicsemi.android.nrfmesh.viewmodel

import android.content.ContentResolver
import android.net.Uri
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
import no.nordicsemi.android.nrfmesh.feature.settings.SettingsListData
import no.nordicsemi.kotlin.mesh.core.exception.NoNetwork
import no.nordicsemi.kotlin.mesh.core.model.Group
import no.nordicsemi.kotlin.mesh.core.model.GroupAddress
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import java.io.BufferedReader
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
        observeNetworkChanges()
    }

    private fun observeNetworkChanges() {
        // Observes the mesh network for any changes i.e. network reset etc.
        repository.network
            .onEach {
                _uiState.update { state ->
                    state.copy(
                        networkState = MeshNetworkState.Success(network = it),
                        counter = state.counter + 1
                    )
                }
                meshNetwork = it
            }
            .launchIn(scope = viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            repository.disconnect()
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

    /**
     * Imports a network from a given Uri.
     *
     * @param uri                  URI of the file.
     * @param contentResolver      Content resolver.
     */
    @OptIn(ExperimentalUuidApi::class)
    internal fun importNetwork(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            val networkJson = contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(inputStream.reader()).use { bufferedReader ->
                    bufferedReader.readText()
                }
            } ?: ""
            val meshNetwork = repository.importMeshNetwork(networkJson.encodeToByteArray())
            this@NetworkViewModel.meshNetwork = meshNetwork
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
            // Let's save the imported network
            repository.save()
        }
    }

    internal fun resetNetwork() {
        viewModelScope.launch { repository.resetNetwork() }
    }

    internal fun nextAvailableGroupAddress(): GroupAddress {
        val provisioner = meshNetwork?.provisioners?.firstOrNull()
        require(provisioner != null) {
            throw IllegalArgumentException("No provisioner found")
        }
        return meshNetwork?.nextAvailableGroup(provisioner)
            ?: throw IllegalArgumentException("No available group address found for ${provisioner.name}")
    }

    fun onAddGroupClicked(group: Group) {
        val meshNetwork = meshNetwork ?: throw NoNetwork()
        meshNetwork.add(group)
        viewModelScope.launch {
            repository.save()
        }
    }
}

internal sealed interface MeshNetworkState {
    data object Loading : MeshNetworkState
    data class Success(val network: MeshNetwork) : MeshNetworkState
}

internal data class NetworkScreenUiState(
    val networkState: MeshNetworkState = MeshNetworkState.Loading,
    val shouldSelectProvisioner: Boolean = false,
    val counter: Int = 0
)