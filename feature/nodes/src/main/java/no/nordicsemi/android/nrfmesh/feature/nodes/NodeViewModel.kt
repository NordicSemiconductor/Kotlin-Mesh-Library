package no.nordicsemi.android.nrfmesh.feature.nodes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.feature.config.networkkeys.configNetKeys
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.feature.nodes.destinations.node
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigCompositionDataGet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigGattProxyGet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigGattProxySet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNodeReset
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Node
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
internal class NodeViewModel @Inject internal constructor(
    private val navigator: Navigator,
    savedStateHandle: SavedStateHandle,
    private val repository: CoreDataRepository
) : SimpleNavigationViewModel(navigator, savedStateHandle) {
    private lateinit var meshNetwork: MeshNetwork
    private lateinit var selectedNode: Node
    private val nodeUuid: UUID = parameterOf(node)

    private val _uiState = MutableStateFlow(NodeScreenUiState())

    val uiState: StateFlow<NodeScreenUiState> = _uiState.asStateFlow()

    init {
        repository.network.onEach {
            meshNetwork = it
            it.node(nodeUuid)?.let { node ->
                this@NodeViewModel.selectedNode = node
                NodeState.Success(node)
            } ?: NodeState.Error(Throwable("Node not found"))
            _uiState.value = _uiState.value.copy(
                nodeState = NodeState.Success(selectedNode)
            )
        }.launchIn(scope = viewModelScope)
    }

    /**
     * Called when the user pulls down to refresh the node details.
     */
    internal fun onRefresh() {
        _uiState.value = uiState.value.copy(isRefreshing = true)
        viewModelScope.launch {
            repository.send(selectedNode, ConfigCompositionDataGet(page = 0x00u))
            _uiState.value = uiState.value.copy(isRefreshing = false)
        }
    }

    /**
     * Called when the user changes the name of the node.
     *
     * @param name New name of the node.
     */
    internal fun onNameChanged(name: String) {
        if (selectedNode.name != name) {
            if (name.isNotEmpty())
                selectedNode.name = name
            else throw IllegalArgumentException("Name cannot be empty")
            viewModelScope.launch {
                repository.save()
            }
        }
    }

    /**
     * Called when the user toggles the proxy state of the node.
     *
     * @param enabled True if proxy is to be enabled or false otherwise.
     */
    internal fun onProxyStateToggled(enabled: Boolean) {
        viewModelScope.launch {
            repository.send(node = selectedNode, message = ConfigGattProxySet(enabled))
        }
    }

    /**
     * Called when the user requests the current proxy state of the node.
     */
    internal fun onGetProxyStateClicked() {
        viewModelScope.launch {
            repository.send(node = selectedNode, message = ConfigGattProxyGet())
        }
    }

    /**
     * Called when the user clicks on the reset node button.
     */
    fun onResetClicked() {
        viewModelScope.launch {
            repository.send(node = selectedNode, message = ConfigNodeReset())?.let {
                navigateUp()
            }
        }
    }

    fun onNetworkKeysClicked() {
        navigator.navigateTo(to = configNetKeys, args = selectedNode.uuid)
    }
}

sealed interface NodeState {
    data class Success(
        val node: Node,
    ) : NodeState

    data class Error(val throwable: Throwable) : NodeState
    data object Loading : NodeState
}

data class NodeScreenUiState internal constructor(
    val nodeState: NodeState = NodeState.Loading,
    val isRefreshing: Boolean = false
)