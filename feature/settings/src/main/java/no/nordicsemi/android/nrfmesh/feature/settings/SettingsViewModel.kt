package no.nordicsemi.android.nrfmesh.feature.settings

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import java.io.BufferedReader
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: CoreDataRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsScreenUiState())
    val uiState: StateFlow<SettingsScreenUiState> = _uiState.asStateFlow()
    private lateinit var network: MeshNetwork

    init {
        viewModelScope.launch {
            repository.network.collect {
                println("Update received in settings vm ${it.timestamp}")
                network = it
                val selectedSetting = _uiState.value.selectedSetting
                _uiState.value = _uiState.value.copy(
                    networkState = MeshNetworkState.Success(
                        network = it,
                        settingsListData = SettingsListData(it)
                    ),
                    selectedSetting = selectedSetting
                )
            }
        }
    }

    internal fun onItemSelected(clickableSetting: ClickableSetting) {
        _uiState.value = _uiState.value.copy(selectedSetting = clickableSetting)
    }

    /**
     * Invoked when the name of the network is changed.
     *
     * @param name Name of the network.
     */
    fun onNameChanged(name: String) {
        network.name = name
        save()
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
        viewModelScope.launch {
            repository.resetNetwork()
        }
    }


    fun save() {
        viewModelScope.launch { repository.save() }
    }
}

sealed interface MeshNetworkState {
    data class Success(
        val network: MeshNetwork,
        val settingsListData: SettingsListData,
    ) : MeshNetworkState
    data object Loading : MeshNetworkState
}

data class SettingsScreenUiState(
    val networkState: MeshNetworkState = MeshNetworkState.Loading,
    val selectedSetting: ClickableSetting? = null,
)