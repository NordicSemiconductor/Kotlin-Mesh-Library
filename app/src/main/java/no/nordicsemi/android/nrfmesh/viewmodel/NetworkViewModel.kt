package no.nordicsemi.android.nrfmesh.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.core.data.storage.MeshSecurePropertiesStorage
import no.nordicsemi.kotlin.mesh.core.model.Group
import no.nordicsemi.kotlin.mesh.core.model.GroupAddress
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import java.io.BufferedReader
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NetworkViewModel @Inject constructor(
    private val repository: CoreDataRepository,
    private val storage: MeshSecurePropertiesStorage,
) : ViewModel() {
    private lateinit var meshNetwork: MeshNetwork
    private val _uiState = MutableStateFlow(NetworkScreenUiState())
    internal val uiState: StateFlow<NetworkScreenUiState> = _uiState.asStateFlow()

    init {
        // Loads a mesh network on view model creation
        loadNetwork()

        // Observes the mesh network for any changes i.e. network reset etc.
        repository.network
            .onEach {
                meshNetwork = it
                _uiState.value = _uiState.value.copy(provisioners = it.provisioners)
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
     * Loads the network
     */
    private fun loadNetwork() {
        viewModelScope.launch {
            meshNetwork = repository.load()
            repository.startAutomaticConnectivity(meshNetwork)
        }
    }

    /**
     * Selects the given provisioner.
     *
     * @param provisioner Provisioner to be selected.
     */
    internal fun onProvisionerSelected(provisioner: Provisioner) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(shouldSelectProvisioner = false)
            meshNetwork.move(provisioner = provisioner, to = 0)
            storage.storeLocalProvisioner(
                uuid = meshNetwork.uuid,
                localProvisionerUuid = provisioner.uuid
            )
            repository.save()
        }
    }

    /**
     * Imports a network from a given Uri.
     *
     * @param uri                  URI of the file.
     * @param contentResolver      Content resolver.
     */
    internal fun importNetwork(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            val networkJson = contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(inputStream.reader()).use { bufferedReader ->
                    bufferedReader.readText()
                }
            } ?: ""
            meshNetwork = repository.importMeshNetwork(networkJson.encodeToByteArray())
            storage.localProvisioner(uuid = meshNetwork.uuid)?.let { uuid ->
                meshNetwork.provisioner(uuid = UUID.fromString(uuid))?.let {
                    meshNetwork.move(provisioner = it, to = 0)
                    storage.storeLocalProvisioner(
                        uuid = meshNetwork.uuid,
                        localProvisionerUuid = it.uuid
                    )
                }
            } ?: run {
                meshNetwork.takeIf { it.provisioners.size > 1 }
                    ?.let {
                        _uiState.value = _uiState.value.copy(
                            provisioners = it.provisioners,
                            shouldSelectProvisioner = true
                        )
                    } ?: run {
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
        val provisioner = meshNetwork.provisioners.firstOrNull()
        require(provisioner != null) { throw IllegalArgumentException("No provisioner found") }
        return meshNetwork.nextAvailableGroup(provisioner)
            ?: throw IllegalArgumentException("No available group address found for ${provisioner.name}")
    }

    fun onAddGroupClicked(group: Group) {
        meshNetwork.add(group)
    }
}

internal data class NetworkScreenUiState(
    val provisioners: List<Provisioner> = emptyList(),
    val shouldSelectProvisioner: Boolean = false,
)