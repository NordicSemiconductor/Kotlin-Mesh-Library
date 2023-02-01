package no.nordicsemi.android.nrfmesh.feature.settings

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.DestinationId
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import java.io.BufferedReader
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val navigator: Navigator,
    private val repository: DataStoreRepository
) : ViewModel() {
    val uiState: StateFlow<SettingsScreenUiState> =
        repository.network.map { network ->
            this@SettingsViewModel.network = network
            SettingsScreenUiState(networkState = MeshNetworkState.Success(network))
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsScreenUiState()
        )

    lateinit var network: MeshNetwork

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
        }
    }

    /**
     * Invoked when the name of the network is changed.
     *
     * @param name Name of the network.
     */
    internal fun onNameChanged(name: String) {
        if (name != network.name) {
            network.name = name
            viewModelScope.launch {
                repository.save()
            }
        }
    }

    internal fun navigate(destinationId: DestinationId<Unit, *>){
        navigator.navigateTo(destinationId)
    }
}

sealed interface MeshNetworkState {
    data class Success(val network: MeshNetwork) : MeshNetworkState
    data class Error(val throwable: Throwable) : MeshNetworkState
    object Loading : MeshNetworkState
}

data class SettingsScreenUiState(val networkState: MeshNetworkState = MeshNetworkState.Loading)