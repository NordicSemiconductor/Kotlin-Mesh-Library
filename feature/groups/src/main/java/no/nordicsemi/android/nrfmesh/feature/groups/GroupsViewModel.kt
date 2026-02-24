package no.nordicsemi.android.nrfmesh.feature.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.kotlin.mesh.core.exception.NoNetwork
import no.nordicsemi.kotlin.mesh.core.model.Group
import no.nordicsemi.kotlin.mesh.core.model.GroupAddress
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import javax.inject.Inject

@HiltViewModel
internal class GroupsViewModel @Inject internal constructor(
    private val repository: CoreDataRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GroupsScreenUiState(groups = listOf()))
    val uiState: StateFlow<GroupsScreenUiState> = _uiState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GroupsScreenUiState(groups = listOf())
        )

    private lateinit var meshNetwork: MeshNetwork

    init {
        viewModelScope.launch {
            repository.network.collect { network ->
                this@GroupsViewModel.meshNetwork = network
                _uiState.value = GroupsScreenUiState(
                    groups = network.groups
                )
            }
        }
    }

    /**
     * Returns the next available group address
     */
    internal fun nextAvailableGroupAddress(): GroupAddress {
        val provisioner = meshNetwork.provisioners.firstOrNull()
        require(provisioner != null) {
            throw IllegalArgumentException("No provisioner found")
        }
        return meshNetwork.nextAvailableGroup(provisioner)
            ?: throw IllegalArgumentException("No available group address found for ${provisioner.name}")
    }

    /**
     * Adds a group to the mesh network
     */
    internal fun addGroup(group: Group) {
        val meshNetwork = meshNetwork ?: throw NoNetwork()
        meshNetwork.add(group)
        viewModelScope.launch {
            repository.save()
        }
    }
}

@ConsistentCopyVisibility
internal data class GroupsScreenUiState internal constructor(
    val groups: List<Group> = listOf(),
)