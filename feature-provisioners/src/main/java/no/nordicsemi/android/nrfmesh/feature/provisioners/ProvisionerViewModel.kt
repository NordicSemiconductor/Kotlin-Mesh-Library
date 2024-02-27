package no.nordicsemi.android.nrfmesh.feature.provisioners

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.feature.provisioners.destinations.provisioner
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
internal class ProvisionerViewModel @Inject internal constructor(
    navigator: Navigator,
    savedStateHandle: SavedStateHandle,
    private val repository: CoreDataRepository
) : SimpleNavigationViewModel(navigator, savedStateHandle) {
    private lateinit var meshNetwork: MeshNetwork
    private val provisionerUuid: UUID = parameterOf(provisioner)

    private val _uiState = MutableStateFlow(ProvisionerScreenUiState(ProvisionerState.Loading))
    val uiState: StateFlow<ProvisionerScreenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.network.collect { network ->
                meshNetwork = network
                _uiState.update { state ->
                    network.provisioner((provisionerUuid))?.let { provisioner ->
                        when (val provisionerState = state.provisionerState) {
                            is ProvisionerState.Loading -> ProvisionerScreenUiState(
                                provisionerState = ProvisionerState.Success(
                                    provisioner = provisioner,
                                    otherProvisioners = network.provisioners.filter {
                                        it != provisioner
                                    }
                                )
                            )

                            is ProvisionerState.Success -> state.copy(
                                provisionerState = provisionerState.copy(
                                    provisioner = provisioner,
                                    otherProvisioners = network.provisioners.filter {
                                        it != provisioner
                                    }
                                )
                            )

                            else -> state
                        }
                    } ?: ProvisionerScreenUiState(
                        provisionerState = ProvisionerState.Error(
                            throwable = Throwable("Provisioner not found")
                        )
                    )
                }
            }
        }
    }

    /**
     * Invoked when the name of the provisioner is changed.
     *
     * @param name New provisioner name.
     */
    internal fun onNameChanged(name: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                val provisionerState = state.provisionerState as ProvisionerState.Success
                provisionerState.provisioner.apply { this.name = name }
                state.copy(provisionerState = provisionerState)
            }
        }
        save()
    }

    /**
     * Checks if the given address is valid
     */
    fun isValidAddress(address: UShort): Boolean = UnicastAddress.isValid(address = address)

    /**
     * Invoked when the name of the provisioner is changed.
     *
     * @param address New address of the provisioner.
     */
    internal fun onAddressChanged(address: Int) {
        val state = _uiState.value.provisionerState as ProvisionerState.Success
        state.provisioner.assign(address = UnicastAddress(address = address))
        save()
    }

    /**
     * Disables the configuration capabilities of a provisioner.
     */
    internal fun disableConfigurationCapabilities() {
        val state = _uiState.value.provisionerState as ProvisionerState.Success
        state.provisioner.disableConfigurationCapabilities()
        save()
    }

    internal fun onTtlChanged(ttl: Int) {
        // TODO "Incomplete implementation, this should be configured by sending a message."
    }

    /**
     * Saves the network.
     */
    private fun save() {
        viewModelScope.launch { repository.save() }
    }
}

sealed interface ProvisionerState {
    data class Success(
        val provisioner: Provisioner,
        val otherProvisioners: List<Provisioner> = listOf()
    ) : ProvisionerState

    data class Error(val throwable: Throwable) : ProvisionerState
    data object Loading : ProvisionerState
}

data class ProvisionerScreenUiState internal constructor(
    val provisionerState: ProvisionerState = ProvisionerState.Loading
)