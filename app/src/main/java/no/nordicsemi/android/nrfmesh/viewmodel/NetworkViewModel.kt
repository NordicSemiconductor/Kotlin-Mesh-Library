package no.nordicsemi.android.nrfmesh.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.kotlin.mesh.core.model.Group
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import java.io.BufferedReader
import javax.inject.Inject

@HiltViewModel
class NetworkViewModel @Inject constructor(
    private val repository: CoreDataRepository,
) : ViewModel() {
    private lateinit var meshNetwork: MeshNetwork

    init {
        // Loads a mesh network on view model creation
        loadNetwork()

        // Observes the mesh network for any changes i.e. network reset etc.
        repository.network
            .onEach { meshNetwork = it }
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
            repository.importMeshNetwork(networkJson.encodeToByteArray())
            // Let's save the imported network
            repository.save()
        }
    }

    internal fun resetNetwork() {
        viewModelScope.launch { repository.resetNetwork() }
    }

    fun onAddGroupClicked(): Group {
        val provisioner = meshNetwork.provisioners.firstOrNull()
        require(provisioner != null) { throw IllegalArgumentException("No provisioner found") }
        return meshNetwork.nextAvailableGroup(provisioner)?.let { address ->
            Group(
                _name = "Group ${meshNetwork.groups.size + 1}",
                address = address
            ).also {
                meshNetwork.add(it)
                save()
            }
        }
            ?: throw IllegalArgumentException("No available group address found for ${provisioner.name}")
    }

    internal fun save() {
        viewModelScope.launch { repository.save() }
    }
}