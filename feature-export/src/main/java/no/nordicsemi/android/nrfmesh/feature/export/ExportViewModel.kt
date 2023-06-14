package no.nordicsemi.android.nrfmesh.feature.export

import android.content.ContentResolver
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import no.nordicsemi.kotlin.mesh.core.model.serialization.config.DeviceKeyConfig
import no.nordicsemi.kotlin.mesh.core.model.serialization.config.NetworkConfiguration
import no.nordicsemi.kotlin.mesh.core.model.serialization.config.NetworkKeysConfig
import no.nordicsemi.kotlin.mesh.core.model.serialization.config.NodesConfig
import no.nordicsemi.kotlin.mesh.core.model.serialization.config.ProvisionersConfig
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject internal constructor(
    savedStateHandle: SavedStateHandle,
    navigator: Navigator,
    private val repository: DataStoreRepository
) : SimpleNavigationViewModel(navigator = navigator, savedStateHandle = savedStateHandle) {

    var uiState by mutableStateOf(ExportScreenUiState())
        private set

    init {
        viewModelScope.launch {
            repository.network.collectLatest { network ->
                uiState = uiState.copy(
                    networkName = network.name,
                    provisionerItemStates = network.provisioners.map { ProvisionerItemState(it) },
                    networkKeyItemStates = network.networkKeys.map { NetworkKeyItemState(it) }
                )
            }
        }
    }

    /**
     * Invoked when export everything is toggled.
     *
     * @param isToggled True if toggled or false otherwise.
     */
    internal fun onExportEverythingToggled(isToggled: Boolean) {
        uiState = uiState.copy(exportEverything = isToggled)
    }

    /**
     * Invoked when a Provisioner is selected/unselected to be exported.
     *
     * @param provisioner     Provisioner.
     * @param selected        True if selected or false otherwise.
     */
    internal fun onProvisionerSelected(provisioner: Provisioner, selected: Boolean) {
        uiState = uiState.copy(
            provisionerItemStates = uiState.provisionerItemStates.map {
                if (it.provisioner.uuid == provisioner.uuid)
                    it.copy(isSelected = selected)
                else it
            }
        )
    }

    /**
     * Invoked when the network keys to exported are selected.
     *
     * @param key          Network key.
     * @param selected     True if selected or false otherwise.
     */
    internal fun onNetworkKeySelected(key: NetworkKey, selected: Boolean) {
        uiState = uiState.copy(
            networkKeyItemStates = uiState.networkKeyItemStates.map {
                if (it.networkKey.index == key.index)
                    it.copy(isSelected = selected)
                else it
            }
        )
    }

    /**
     * Updates the ui state when when device keys check box is toggled.
     *
     * @param isToggled True if toggled and false otherwise.
     */
    internal fun onExportDeviceKeysToggled(isToggled: Boolean) {
        uiState = uiState.copy(/*exportState = ExportState.Unknown,*/ exportDeviceKeys = isToggled)
    }

    /**
     * Invoked when the current export state is displayed to the user.
     */
    internal fun onExportStateDisplayed() {
        uiState = uiState.copy(exportState = ExportState.Unknown)
    }

    /**
     * Exports the mesh network based on the selected configuration.
     * @param contentResolver Content resolver
     * @param uri             Uri of the file.
     */
    fun export(contentResolver: ContentResolver, uri: Uri) {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            uiState = uiState.copy(exportState = ExportState.Error(throwable))
        }) {
            repository.network.collectLatest { network ->
                uiState.run {
                    val data = repository.exportNetwork(
                        configuration = when (exportEverything) {
                            true -> NetworkConfiguration.Full
                            false -> createPartialConfiguration(
                                network = network,
                                networkKeyItemStates = networkKeyItemStates,
                                provisionerItemStates = provisionerItemStates,
                                exportDeviceKeys = exportDeviceKeys
                            )
                        }
                    )
                    contentResolver.openOutputStream(uri)?.run {
                        write(data)
                        close()
                    }
                }
                uiState = uiState.copy(exportState = ExportState.Success)
            }
        }
    }

    /**
     * Creates a partial network configuration for a given mesh network.
     *
     * @param network                     Mesh network to be exported.
     * @param networkKeyItemStates        Network keys UI state.
     * @param provisionerItemStates       Provisioners UI state.
     * @param exportDeviceKeys            Specifies if device keys should be exported.
     */
    private fun createPartialConfiguration(
        network: MeshNetwork,
        networkKeyItemStates: List<NetworkKeyItemState>,
        provisionerItemStates: List<ProvisionerItemState>,
        exportDeviceKeys: Boolean
    ) = NetworkConfiguration.Partial(
        networkKeysConfig = networkKeyConfiguration(
            network = network,
            selectedNetworkKeys = networkKeyItemStates.filter { it.isSelected }
        ),
        provisionersConfig = provisionerConfiguration(
            network = network,
            selectedProvisioners = provisionerItemStates.filter { it.isSelected }
        ),
        nodesConfig = NodesConfig.All(
            deviceKeyConfig = when (exportDeviceKeys) {
                true -> DeviceKeyConfig.INCLUDE_KEY
                false -> DeviceKeyConfig.EXCLUDE_KEY
            }
        )
    )

    /**
     * Creates the provisioner configuration for a given mesh network based on user selection.
     *
     * @param network                Mesh network to be exported.
     * @param selectedProvisioners   Provisioners to be exported.
     */
    private fun provisionerConfiguration(
        network: MeshNetwork,
        selectedProvisioners: List<ProvisionerItemState>
    ): ProvisionersConfig = when (selectedProvisioners.size == network.provisioners.size) {
        true -> ProvisionersConfig.All
        false -> ProvisionersConfig.Some(selectedProvisioners.map { it.provisioner })
    }

    /**
     * Creates the network configuration for a given mesh network based on user selection.
     *
     * @param network                Mesh network to be exported.
     * @param selectedNetworkKeys    Network keys that are selected to be exported.
     */
    private fun networkKeyConfiguration(
        network: MeshNetwork,
        selectedNetworkKeys: List<NetworkKeyItemState>
    ): NetworkKeysConfig = when (selectedNetworkKeys.size == network.networkKeys.size) {
        true -> NetworkKeysConfig.All
        false -> NetworkKeysConfig.Some(selectedNetworkKeys.map { it.networkKey })
    }
}

sealed interface ExportState {
    object Success : ExportState
    data class Error(val throwable: Throwable) : ExportState
    object Unknown : ExportState
}

data class ExportScreenUiState internal constructor(
    val exportState: ExportState = ExportState.Unknown,
    val exportEverything: Boolean = true,
    val networkName: String = "Mesh Network",
    val provisionerItemStates: List<ProvisionerItemState> = listOf(),
    val networkKeyItemStates: List<NetworkKeyItemState> = listOf(),
    val exportDeviceKeys: Boolean = true
) {
    val enableExportButton: Boolean
        get() = when (exportEverything) {
            true -> true
            false -> provisionerItemStates.any { it.isSelected } &&
                    networkKeyItemStates.any { it.isSelected }
        }
}

data class ProvisionerItemState internal constructor(
    val provisioner: Provisioner,
    val isSelected: Boolean = false
)

data class NetworkKeyItemState internal constructor(
    val networkKey: NetworkKey,
    val isSelected: Boolean = false
)