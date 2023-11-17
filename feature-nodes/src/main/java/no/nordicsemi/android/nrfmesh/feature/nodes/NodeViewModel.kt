package no.nordicsemi.android.nrfmesh.feature.nodes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.DestinationId
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.feature.nodes.destinations.node
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigCompositionDataGet
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Node
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
internal class NodeViewModel @Inject internal constructor(
    navigator: Navigator,
    savedStateHandle: SavedStateHandle,
    private val repository: CoreDataRepository
) : SimpleNavigationViewModel(navigator, savedStateHandle) {
    private lateinit var meshNetwork: MeshNetwork
    private lateinit var selectedNode: Node
    private val nodeUuid: UUID = parameterOf(node)

    val uiState: StateFlow<NodeScreenUiState> = repository.network.map {
        meshNetwork = it
        it.node(nodeUuid)?.let { node ->
            this@NodeViewModel.selectedNode = node
            NodeState.Success(node)
        } ?: NodeState.Error(Throwable("Node not found"))
        NodeScreenUiState(
            nodeState = NodeState.Success(selectedNode)
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        NodeScreenUiState()
    )

    internal fun send(node: Node) {
        viewModelScope.launch {
            repository.sendMessage(
                node = node,
                message = ConfigCompositionDataGet(page = 0x00u)
            )
        }
    }

    internal fun onNameChanged(name: String) {
        if(selectedNode.name != name) {
            if (name.isNotEmpty())
                selectedNode.name = name
            else throw IllegalArgumentException("Name cannot be empty")
            viewModelScope.launch {
                repository.save()
            }
        }
    }

    internal fun navigateTo(destination: DestinationId<Unit, Unit>) {

    }
}

sealed interface NodeState {
    data class Success(
        val node: Node
    ) : NodeState

    data class Error(val throwable: Throwable) : NodeState
    data object Loading : NodeState
}

data class NodeScreenUiState internal constructor(
    val nodeState: NodeState = NodeState.Loading
)