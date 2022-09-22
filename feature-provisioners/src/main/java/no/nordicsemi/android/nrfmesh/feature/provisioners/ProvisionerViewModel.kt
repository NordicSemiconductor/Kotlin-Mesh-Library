package no.nordicsemi.android.nrfmesh.feature.provisioners

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.ProvisionerDestination
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import java.util.*
import javax.inject.Inject

@HiltViewModel
internal class ProvisionerViewModel @Inject internal constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: DataStoreRepository
) : ViewModel() {
    private lateinit var provisioner: Provisioner
    private val provisionerUuid: String =
        checkNotNull(savedStateHandle[ProvisionerDestination.provisionerUuidArg])

    val uiState: StateFlow<ProvisionerScreenUiState> = repository.network.map { network ->
        network.provisioner(UUID.fromString(provisionerUuid))?.let {
            this@ProvisionerViewModel.provisioner = it
            ProvisionerScreenUiState(
                provisionerState = ProvisionerState.Success(provisioner = provisioner)
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
        if (provisioner.name != name) {
            provisioner.name = name
            save()
        }
    }

    /**
     * Invoked when the name of the provisioner is changed.
     *
     * @param address New address of the provisioner.
     */
    internal fun onAddressChanged(address: String) {
        /*if (provisioner.name != address) {
            provisioner.name = address
            save()
        }*/
    }

    /**
     * Saves the network.
     */
    internal fun save() {
        viewModelScope.launch { repository.save() }
    }
}

sealed interface ProvisionerState {
    data class Success(val provisioner: Provisioner) : ProvisionerState
    data class Error(val throwable: Throwable) : ProvisionerState
    object Loading : ProvisionerState
}

@Suppress("ArrayInDataClass")
data class ProvisionerScreenUiState internal constructor(
    val provisionerState: ProvisionerState = ProvisionerState.Loading
)