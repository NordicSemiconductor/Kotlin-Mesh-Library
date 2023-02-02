package no.nordicsemi.android.nrfmesh.feature.provisioners

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.DestinationId
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import java.util.*
import javax.inject.Inject

@HiltViewModel
internal class ProvisionersViewModel @Inject internal constructor(
    savedStateHandle: SavedStateHandle,
    private val navigator: Navigator,
    private val repository: DataStoreRepository
) : SimpleNavigationViewModel(navigator, savedStateHandle) {
    private val _uiState = MutableStateFlow(ProvisionersScreenUiState(listOf()))
    val uiState: StateFlow<ProvisionersScreenUiState> = _uiState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ProvisionersScreenUiState()
    )

    private lateinit var network: MeshNetwork
    private var provisionersToBeRemoved = mutableListOf<Provisioner>()

    init {
        viewModelScope.launch {
            repository.network.collect { network ->
                this@ProvisionersViewModel.network = network
                _uiState.value = ProvisionersScreenUiState(
                    provisioners = filterProvisionersTobeRemoved()
                )
            }
        }
    }

    internal fun navigate(destinationId: DestinationId<UUID, Unit>, uuid: UUID) {
        navigator.navigateTo(destinationId, uuid)
    }

    /**
     * Adds a scene to the network.
     */
    internal fun addProvisioner(): Provisioner {
        removeProvisioners()
        val provisioner = Provisioner()
        network.run {
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
        return provisioner
    }

    /**
     * Invoked when a provisioner is swiped to be deleted. The given provisioner is added to a list
     * of provisioners to be deleted.
     *
     * @param provisioner Provisioner to be deleted.
     */
    internal fun onSwiped(provisioner: Provisioner) {
        if (!provisionersToBeRemoved.contains(provisioner))
            provisionersToBeRemoved.add(provisioner)
        if (provisionersToBeRemoved.size == network.scenes.size)
            _uiState.value =
                ProvisionersScreenUiState(provisioners = filterProvisionersTobeRemoved())
    }

    /**
     * Invoked when a provisioner is swiped to be deleted is undone. When invoked the given
     * provisioner is removed from the list of provisioners to be deleted.
     *
     * @param provisioner Scene to be reverted.
     */
    internal fun onUndoSwipe(provisioner: Provisioner) {
        provisionersToBeRemoved.remove(provisioner)
        if (provisionersToBeRemoved.isEmpty()) {
            _uiState.value =
                ProvisionersScreenUiState(provisioners = filterProvisionersTobeRemoved())
        }
    }

    /**
     * Remove a given scene from the network.
     *
     * @param provisioner Scene to be removed.
     */
    internal fun remove(provisioner: Provisioner) {
        network.apply {
            provisioners.find { it == provisioner }?.let {
                remove(it)
            }
        }
        provisionersToBeRemoved.remove(provisioner)
    }

    /**
     * Removes the scene from a network.
     */
    internal fun removeProvisioners() {
        remove()
        save()
    }

    /**
     * Removes the selected provisioners from the network.
     */
    private fun remove() {
        network.provisioners.filter {
            it in provisionersToBeRemoved
        }.forEach {
            network.remove(it)
        }
        provisionersToBeRemoved.clear()
    }

    /**
     * Saves the network.
     */
    private fun save() {
        viewModelScope.launch { repository.save() }
    }

    private fun filterProvisionersTobeRemoved() = network.provisioners.filter {
        it !in provisionersToBeRemoved
    }
}

data class ProvisionersScreenUiState internal constructor(
    val provisioners: List<Provisioner> = listOf()
)
