package no.nordicsemi.android.nrfmesh.feature.provisioners

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import no.nordicsemi.android.nrfmesh.feature.provisioners.destinations.provisioner
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import java.util.*
import javax.inject.Inject

@HiltViewModel
internal class ProvisionerViewModel @Inject internal constructor(
    navigator: Navigator,
    savedStateHandle: SavedStateHandle,
    private val repository: DataStoreRepository
) : SimpleNavigationViewModel(navigator, savedStateHandle) {
    private lateinit var meshNetwork: MeshNetwork
    private lateinit var selectedProvisioner: Provisioner
    private val provisionerUuid: UUID = parameterOf(provisioner)

    val uiState: StateFlow<ProvisionerScreenUiState> = repository.network.map { network ->
        meshNetwork = network
        network.provisioner((provisionerUuid))?.let { provisioner ->
            this@ProvisionerViewModel.selectedProvisioner = provisioner
            ProvisionerScreenUiState(
                provisionerState = ProvisionerState.Success(
                    provisioner = provisioner,
                    otherProvisioners = network.provisioners.filter { it != provisioner }
                )
            )
        } ?: ProvisionerScreenUiState(
            provisionerState = ProvisionerState.Error(
                throwable = Throwable("Provisioner not found")
            )
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ProvisionerScreenUiState(ProvisionerState.Loading)
    )

    /**
     * Invoked when the name of the provisioner is changed.
     *
     * @param name New provisioner name.
     */
    internal fun onNameChanged(name: String) {
        if (selectedProvisioner.name != name) {
            selectedProvisioner.name = name
            save()
        }
    }

    /**
     * Invoked when the name of the provisioner is changed.
     *
     * @param address New address of the provisioner.
     */
    internal fun onAddressChanged(address: Int) = runCatching {
        val newAddress = UnicastAddress(address = address)
        selectedProvisioner.assign(address = newAddress)
    }.onSuccess {
        save()
    }

    /**
     * Disables the configuration capabilities of a provisioner.
     */
    internal fun disableConfigurationCapabilities(): Result<Unit> = runCatching {
        meshNetwork.disableConfigurationCapabilities(selectedProvisioner)
    }.onSuccess {
        save()
    }

    internal fun onTtlChanged(ttl: Int) {
        TODO("Incomplete implementation, this should be configured by sending a message.")
    }

    /**
     * Saves the network.
     */
    internal fun save() {
        viewModelScope.launch { repository.save() }
    }
}

sealed interface ProvisionerState {
    data class Success(
        val provisioner: Provisioner,
        val otherProvisioners: List<Provisioner> = listOf()
    ) : ProvisionerState

    data class Error(val throwable: Throwable) : ProvisionerState
    object Loading : ProvisionerState
}

@Suppress("ArrayInDataClass")
data class ProvisionerScreenUiState internal constructor(
    val provisionerState: ProvisionerState = ProvisionerState.Loading
)