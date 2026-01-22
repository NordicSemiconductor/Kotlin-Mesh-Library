package no.nordicsemi.android.nrfmesh.feature.nodes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Node
import javax.inject.Inject

@HiltViewModel
internal class NodesViewModel @Inject internal constructor(
    private val repository: CoreDataRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(value = NodesScreenUiState())
    val uiState: StateFlow<NodesScreenUiState> = _uiState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NodesScreenUiState()
    )

    private lateinit var network: MeshNetwork

    init {
        observerNetworkChanges()
    }

    internal fun observerNetworkChanges() {
        repository.network.onEach { network ->
            this.network = network
            _uiState.value = NodesScreenUiState(
                nodes = network.nodes.toList()
            )
        }.launchIn(scope = viewModelScope)

    }
}

internal data class NodesScreenUiState(val nodes: List<Node> = listOf())