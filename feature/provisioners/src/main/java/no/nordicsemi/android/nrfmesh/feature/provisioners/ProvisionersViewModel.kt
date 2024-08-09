package no.nordicsemi.android.nrfmesh.feature.provisioners

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
internal class ProvisionersViewModel @Inject internal constructor(
    private val repository: CoreDataRepository
) : ViewModel() {

    private lateinit var network: MeshNetwork

    private val _uiState = MutableStateFlow(ProvisionersScreenUiState(listOf()))
    val uiState: StateFlow<ProvisionersScreenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.network.collect { network ->
                this@ProvisionersViewModel.network = network
                _uiState.update { state ->
                    val provisioners = network.provisioners.toList()
                    state.copy(
                        provisioners = provisioners,
                        provisionersToBeRemoved = provisioners.filter {
                            it in state.provisionersToBeRemoved
                        }
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        removeProvisioners()
    }

    /**
     * Adds a scene to the network.
     */
    internal fun addProvisioner() = Provisioner().apply {
        name = repository.createProvisionerName()
    }.also { provisioner ->
        network.apply {
            nextAvailableUnicastAddressRange(rangeSize = 0x199A)?.let { range ->
                provisioner.allocate(range)
            }
            nextAvailableGroupAddressRange(rangeSize = 0x0C9A)?.let { range ->
                provisioner.allocate(range)
            }
            nextAvailableSceneRange(rangeSize = 0x3334)?.let { range ->
                provisioner.allocate(range)
            }
            add(provisioner = provisioner, address = null)
        }
    }


    /**
     * Invoked when a provisioner is swiped to be deleted. The given provisioner is added to a list
     * of provisioners to be deleted.
     *
     * @param provisioner Provisioner to be deleted.
     */
    internal fun onSwiped(provisioner: Provisioner) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(provisionersToBeRemoved = it.provisionersToBeRemoved + provisioner)
            }
        }
    }

    /**
     * Invoked when a provisioner is swiped to be deleted is undone. When invoked the given
     * provisioner is removed from the list of provisioners to be deleted.
     *
     * @param provisioner Scene to be reverted.
     */
    internal fun onUndoSwipe(provisioner: Provisioner) {
        _uiState.update {
            it.copy(provisionersToBeRemoved = it.provisionersToBeRemoved - provisioner)
        }
    }

    /**
     * Remove a given scene from the network.
     *
     * @param provisioner Scene to be removed.
     */
    internal fun remove(provisioner: Provisioner) {
        _uiState.update {
            it.copy(provisionersToBeRemoved = it.provisionersToBeRemoved - provisioner)
        }
        network.remove(provisioner)
        save()
    }

    /**
     * Removes the provisioners that are queued for deletion.
     */
    private fun removeProvisioners() {
        _uiState.value.provisionersToBeRemoved.forEach {
            network.remove(it)
        }
        save()
    }

    /**
     * Saves the network.
     */
    private fun save() {
        viewModelScope.launch { repository.save() }
    }
}

data class ProvisionersScreenUiState internal constructor(
    val provisioners: List<Provisioner> = listOf(),
    val provisionersToBeRemoved: List<Provisioner> = listOf()
)
