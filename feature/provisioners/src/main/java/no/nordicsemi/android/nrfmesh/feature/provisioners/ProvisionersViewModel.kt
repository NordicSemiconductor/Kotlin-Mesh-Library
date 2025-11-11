package no.nordicsemi.android.nrfmesh.feature.provisioners

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.core.data.models.ProvisionerData
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import kotlin.uuid.Uuid
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@HiltViewModel
internal class ProvisionersViewModel @Inject internal constructor(
    private val repository: CoreDataRepository,
) : ViewModel() {

    private lateinit var network: MeshNetwork
    private var selectedProvisioner: Uuid? = null

    private val _uiState = MutableStateFlow(ProvisionersScreenUiState())
    val uiState: StateFlow<ProvisionersScreenUiState> = _uiState.asStateFlow()

    init {
        repository.network.onEach { network ->
            this.network = network
            val provisioners = network.provisioners.map { ProvisionerData(it) }
            _uiState.value = ProvisionersScreenUiState(provisioners = provisioners)
        }.launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        removeProvisioners()
    }

    /**
     * Adds a provisioner to the network.
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
            save()
        }
    }


    /**
     * Invoked when a provisioner is swiped to be deleted. The given provisioner is added to a list
     * of provisioners to be deleted.
     *
     * @param provisioner Provisioner to be deleted.
     */
    internal fun onSwiped(provisioner: ProvisionerData) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    provisioners = it.provisioners - provisioner,
                    provisionersToBeRemoved = it.provisionersToBeRemoved + provisioner
                )
            }
        }
    }

    /**
     * Invoked when a provisioner is swiped to be deleted is undone. When invoked the given
     * provisioner is removed from the list of provisioners to be deleted.
     *
     * @param provisioner Scene to be reverted.
     */
    internal fun onUndoSwipe(provisioner: ProvisionerData) {
        viewModelScope.launch {
            val state = _uiState.value
            network.provisioners
                .indexOfFirst { it.uuid == provisioner.uuid }
                .takeIf { it >= 0 }
                ?.let { index ->
                    val provisioners = state.provisioners
                        .toMutableList()
                        .also { it.add(index, provisioner) }
                        .toList()
                    _uiState.value = state.copy(
                        provisioners = provisioners,
                        provisionersToBeRemoved = state.provisionersToBeRemoved - provisioner
                    )
                }
        }
    }

    /**
     * Remove a given scene from the network.
     *
     * @param item Scene to be removed.
     */
    internal fun remove(item: ProvisionerData) {
        viewModelScope.launch {
            val state = _uiState.value
            network.run {
                provisioner(uuid = item.uuid)?.let { provisioner ->
                    remove(provisioner)
                    save()
                }
            }
            _uiState.value = state.copy(
                provisionersToBeRemoved = state.provisionersToBeRemoved - item
            )
        }
    }

    /**
     * Removes the provisioners that are queued for deletion.
     */
    private fun removeProvisioners() {
        _uiState.value.provisionersToBeRemoved.forEach { item ->
            network.run {
                provisioner(uuid = item.uuid)?.let { provisioner ->
                    remove(provisioner)
                }
            }
        }
        save()
    }

    /**
     * Saves the network.
     */
    private fun save() {
        viewModelScope.launch { repository.save() }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun selectProvisioner(uuid: Uuid) {
        selectedProvisioner = uuid
    }

    fun isCurrentlySelectedProvisioner(uuid: Uuid) = selectedProvisioner == uuid
}

@ConsistentCopyVisibility
data class ProvisionersScreenUiState internal constructor(
    val provisioners: List<ProvisionerData> = listOf(),
    val provisionersToBeRemoved: List<ProvisionerData> = listOf(),
)