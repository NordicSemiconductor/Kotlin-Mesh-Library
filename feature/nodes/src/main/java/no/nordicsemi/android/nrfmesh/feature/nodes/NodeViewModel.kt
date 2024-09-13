package no.nordicsemi.android.nrfmesh.feature.nodes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.common.Completed
import no.nordicsemi.android.nrfmesh.core.common.Failed
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.core.common.NotStarted
import no.nordicsemi.android.nrfmesh.core.common.Sending
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigResponse
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
    savedStateHandle: SavedStateHandle,
    private val repository: CoreDataRepository,
) : ViewModel() {
    private lateinit var meshNetwork: MeshNetwork
    private lateinit var selectedNode: Node
    private val nodeUuid: UUID = checkNotNull(savedStateHandle[MeshNavigationDestination.ARG]).let {
        UUID.fromString(it as String)
    }

    private val _uiState = MutableStateFlow(NodeScreenUiState())

    val uiState: StateFlow<NodeScreenUiState> = _uiState.asStateFlow()

    init {
        repository.network.onEach {
            meshNetwork = it
            val state = it.node(nodeUuid)?.let { node ->
                this@NodeViewModel.selectedNode = node
                NodeState.Success(node)
            } ?: NodeState.Error(Throwable("Node not found"))
            _uiState.value = _uiState.value.copy(
                nodeState = state
            )
        }.launchIn(scope = viewModelScope)
    }

    /**
     * Called when the user pulls down to refresh the node details.
     */
    internal fun onRefresh() {
        _uiState.value = uiState.value.copy(isRefreshing = true)
        viewModelScope.launch {
            send(message = ConfigCompositionDataGet(page = 0x00u))
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
            send(message = ConfigGattProxySet(enabled))
        }
    }

    /**
     * Called when the user requests the current proxy state of the node.
     */
    internal fun onGetProxyStateClicked() {
        viewModelScope.launch {
            send(message = ConfigGattProxyGet())
        }
    }

    /**
     * Called when the user clicks on the excluded elements.
     *
     * @param exclude True to exclude the node, false to not exclude from the network.
     */
    internal fun onExcluded(exclude: Boolean) {
        // println("Excluded: $exclude")
        selectedNode.excluded = exclude
        viewModelScope.launch {
            repository.save()
        }
    }

    /**
     * Called when the user clicks on the reset node button.
     */
    fun onResetClicked() {
        viewModelScope.launch {
            send(ConfigNodeReset())
        }
    }

    private fun send(message: AcknowledgedConfigMessage) {
        val handler = CoroutineExceptionHandler { _, throwable ->
            _uiState.value = _uiState.value.copy(
                messageState = Failed(message = message, error = throwable),
                isRefreshing = false,
                showProgress = false
            )
        }
        _uiState.value = _uiState.value.copy(messageState = Sending(message = message))
        viewModelScope.launch(context = handler) {
            repository.send(selectedNode, message)?.let { response ->
                _uiState.value = _uiState.value.copy(
                    messageState = Completed(
                        message = message,
                        response = response as ConfigResponse
                    ),
                    isRefreshing = false,
                    showProgress = false
                )
            } ?: run {
                _uiState.value = _uiState.value.copy(
                    messageState = Failed(
                        message = message,
                        error = IllegalStateException("No response received")
                    ),
                    isRefreshing = false,
                    showProgress = false
                )
            }
        }
    }
}

sealed interface NodeState {

    data object Loading : NodeState

    data class Success(val node: Node) : NodeState

    data class Error(val throwable: Throwable) : NodeState
}

data class NodeScreenUiState internal constructor(
    val nodeState: NodeState = NodeState.Loading,
    val isRefreshing: Boolean = false,
    val showProgress: Boolean = false,
    val messageState: MessageState = NotStarted
)