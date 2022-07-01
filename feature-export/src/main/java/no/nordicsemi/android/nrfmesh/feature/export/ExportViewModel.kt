package no.nordicsemi.android.nrfmesh.feature.export

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject internal constructor(
    private val repository: DataStoreRepository
) : ViewModel() {

    var exportUiState by mutableStateOf(ExportUiState())
        private set

    init {
        viewModelScope.launch {
            repository.network.collectLatest { network ->
                exportUiState = exportUiState.copy(
                    provisionerItemStates = network.provisioners.map { ProvisionerItemState(it) },
                    networkKeyItemStates = network.networkKeys.map { NetworkKeyItemState(it) }
                )
            }
        }
    }

    fun onExportEverythingToggled(isToggled: Boolean) {
        exportUiState = exportUiState.copy(exportEverything = isToggled)
    }

    fun onProvisionerSelected(provisioner: Provisioner, selected: Boolean) {
        exportUiState = exportUiState.copy(
            provisionerItemStates = exportUiState.provisionerItemStates.map {
                if (it.provisioner.uuid == provisioner.uuid)
                    it.copy(isSelected = selected)
                else it
            })
    }

    fun onNetworkKeySelected(key: NetworkKey, selected: Boolean) {
        exportUiState = exportUiState.copy(
            networkKeyItemStates = exportUiState.networkKeyItemStates.map {
                if (it.networkKey.index == key.index)
                    it.copy(isSelected = selected)
                else it
            })
    }

    fun onExportDeviceKeysToggled(isToggled: Boolean) {
        exportUiState = exportUiState.copy(exportDeviceKeys = isToggled)
    }
}

data class ExportUiState internal constructor(
    val exportEverything: Boolean = true,
    val provisionerItemStates: List<ProvisionerItemState> = listOf(),
    val networkKeyItemStates: List<NetworkKeyItemState> = listOf(),
    val exportDeviceKeys: Boolean = true,
)

data class ProvisionerItemState internal constructor(
    val provisioner: Provisioner,
    val isSelected: Boolean = false
)

data class NetworkKeyItemState internal constructor(
    val networkKey: NetworkKey,
    val isSelected: Boolean = false
)