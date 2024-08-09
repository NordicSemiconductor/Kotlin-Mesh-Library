package no.nordicsemi.android.nrfmesh.feature.nodes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Node
import javax.inject.Inject

@HiltViewModel
internal class NodesViewModel @Inject internal constructor(
    private val repository: CoreDataRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NodesScreenUiState(listOf()))
    val uiState: StateFlow<NodesScreenUiState> = _uiState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NodesScreenUiState()
    )

    private lateinit var network: MeshNetwork

    init {
        viewModelScope.launch {
            repository.network.collect { network ->
                this@NodesViewModel.network = network
                _uiState.value = NodesScreenUiState(
                    nodes = network.nodes
                )
            }
        }
    }

    fun navigate(selectedNode: Node) {
        // navigateTo(node, selectedNode.uuid)
    }
}


data class NodesScreenUiState internal constructor(
    val nodes: List<Node> = listOf()
)