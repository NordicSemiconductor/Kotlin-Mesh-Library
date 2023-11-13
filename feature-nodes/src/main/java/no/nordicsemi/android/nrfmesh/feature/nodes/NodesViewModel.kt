package no.nordicsemi.android.nrfmesh.feature.nodes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigCompositionDataGet
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Node
import javax.inject.Inject

@HiltViewModel
internal class NodesViewModel @Inject internal constructor(
    navigator: Navigator,
    savedStateHandle: SavedStateHandle,
    private val repository: CoreDataRepository
) : SimpleNavigationViewModel(navigator, savedStateHandle) {
    private val _uiState = MutableStateFlow(NodesScreenUiState(listOf()))
    val uiState: StateFlow<NodesScreenUiState> = _uiState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        NodesScreenUiState()
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

    internal fun send(node: Node) {
        viewModelScope.launch {
            repository.sendMessage(
                node = node,
                message = ConfigCompositionDataGet(page = 0x00u)/*ConfigGattProxyGet*/
            )
        }
    }
}


data class NodesScreenUiState internal constructor(
    val nodes: List<Node> = listOf()
)