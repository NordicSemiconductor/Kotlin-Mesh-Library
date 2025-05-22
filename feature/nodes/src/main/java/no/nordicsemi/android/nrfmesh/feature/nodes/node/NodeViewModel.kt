package no.nordicsemi.android.nrfmesh.feature.nodes.node

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.common.Completed
import no.nordicsemi.android.nrfmesh.core.common.Failed
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.core.common.NodeIdentityStatus
import no.nordicsemi.android.nrfmesh.core.common.NotStarted
import no.nordicsemi.android.nrfmesh.core.common.Sending
import no.nordicsemi.android.nrfmesh.core.common.unknownApplicationKeys
import no.nordicsemi.android.nrfmesh.core.common.unknownNetworkKeys
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.core.data.NetworkConnectionState
import no.nordicsemi.android.nrfmesh.feature.nodes.node.navigation.NodeRoute
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigResponse
import no.nordicsemi.kotlin.mesh.core.messages.MeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.MeshResponse
import no.nordicsemi.kotlin.mesh.core.messages.UnacknowledgedMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigCompositionDataGet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNodeIdentityGet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNodeIdentityStatus
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
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
    private val nodeUuid = savedStateHandle.toRoute<NodeRoute>().let {
        UUID.fromString(it.uuid)
    }

    private val _uiState = MutableStateFlow(NodeScreenUiState())
    val uiState: StateFlow<NodeScreenUiState> = _uiState.asStateFlow()

    init {
        repository.network.onEach {
            val state = it.node(nodeUuid)?.let { node ->
                this@NodeViewModel.selectedNode = node
                NodeState.Success(
                    node = node,
                    nodeInfoListData = NodeInfoListData(node = node)
                )
            } ?: NodeState.Error(Throwable("Node not found"))
            _uiState.value = _uiState.value.copy(
                nodeState = state,
                availableNetworkKeys = selectedNode.unknownNetworkKeys(),
                availableAppKeys = selectedNode.unknownApplicationKeys()
            )
            meshNetwork = it // update the local network instance
        }.launchIn(scope = viewModelScope)

        // Request the composition data when the network is connected if it has not been requested yet.
        repository.proxyConnectionStateFlow.onEach {
            if (it.connectionState is NetworkConnectionState.Connected) {
                if (!selectedNode.isCompositionDataReceived) {
                    onRefresh()
                }
            }
        }.launchIn(scope = viewModelScope)
    }

    /**
     * Returns if the NodeIdentityState for this should be updated/refreshed.
     *
     * @return true if the NodeIdentityState should be updated, false otherwise.
     */
    private fun shouldUpdateNodeIdentityState(): Boolean =
        _uiState.value.nodeIdentityStates.isEmpty()

    /**
     * Creates a list of NodeIdentityStatus objects for each network key.
     *
     * @return List of NodeIdentityStatus objects.
     */
    private fun createNodeIdentityStates(model: Model) =
        model.parentElement?.parentNode?.networkKeys
            ?.map { key ->
                NodeIdentityStatus(
                    networkKey = key,
                    nodeIdentityState = null
                )
            } ?: emptyList()

    /**
     * Called when the user pulls down to refresh the node details.
     */
    internal fun onRefresh() {
        _uiState.value = uiState.value.copy(isRefreshing = true)
        send(message = ConfigCompositionDataGet(page = 0x00u))
    }

    /**
     * Called when the user clicks on the excluded elements.
     *
     * @param exclude True to exclude the node, false to not exclude from the network.
     */
    internal fun onExcluded(exclude: Boolean) {
        selectedNode.excluded = exclude
        viewModelScope.launch {
            repository.save()
        }
    }

    internal fun onItemSelected(item: ClickableNodeInfoItem) {
        _uiState.value = _uiState.value.copy(selectedNodeInfoItem = item)
    }

    internal fun send(message: AcknowledgedConfigMessage) {
        _uiState.value = _uiState.value.copy(messageState = Sending(message = message))
        viewModelScope.launch {
            try {
                repository.send(selectedNode, message)?.let { response ->
                    _uiState.value = _uiState.value.copy(
                        messageState = Completed(
                            message = message,
                            response = response as ConfigResponse
                        ),
                        isRefreshing = false
                    )
                } ?: run {
                    _uiState.value = _uiState.value.copy(
                        messageState = Failed(
                            message = message,
                            error = IllegalStateException("No response received")
                        ),
                        isRefreshing = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    messageState = Failed(message = message, error = e),
                    isRefreshing = false
                )
            }
        }
    }

    internal fun send(model: Model, message: MeshMessage) {
        _uiState.value = _uiState.value.copy(messageState = Sending(message = message))
        viewModelScope.launch {
            runCatching {
                if (message is AcknowledgedMeshMessage) {
                    val response = repository.send(model = model, ackedMessage = message)
                    _uiState.value = _uiState.value.copy(
                        messageState = Completed(
                            message = message,
                            response = response as? MeshResponse
                        )
                    )

                } else {
                    repository.send(
                        model = model,
                        unackedMessage = message as UnacknowledgedMeshMessage
                    )
                    _uiState.value =
                        _uiState.value.copy(messageState = Completed(message = message))
                }
            }.getOrElse {
                _uiState.value = _uiState.value.copy(
                    messageState = Failed(message = message, error = it),
                    isRefreshing = false
                )
            }
        }
    }

    internal fun requestNodeIdentityStates(model: Model) {
        viewModelScope.launch {
            val element = model.parentElement ?: throw IllegalStateException("Element not found")
            if (shouldUpdateNodeIdentityState()) {
                _uiState.value = _uiState.value.copy(
                    nodeIdentityStates = createNodeIdentityStates(model = model)
                )
            }
            val uiState = _uiState.value
            val nodeIdentityStates = uiState.nodeIdentityStates.toMutableList()
            val keys = element.parentNode?.networkKeys ?: emptyList()

            var message: ConfigNodeIdentityGet? = null
            var response: ConfigNodeIdentityStatus? = null
            try {
                keys.forEach { key ->
                    message = ConfigNodeIdentityGet(index = key.index)
                    _uiState.value = _uiState.value.copy(messageState = Sending(message = message))
                    response = repository.send(
                        node = element.parentNode!!,
                        message = message
                    ) as ConfigNodeIdentityStatus

                    response.let { status ->
                        val index = nodeIdentityStates.indexOfFirst { state ->
                            state.networkKey.index == status.index
                        }
                        nodeIdentityStates[index] = nodeIdentityStates[index]
                            .copy(nodeIdentityState = status.identity)
                    }
                }
                _uiState.value = _uiState.value.copy(
                    messageState = Completed(
                        message = ConfigNodeIdentityGet(index = keys.first().index),
                        response = response as ConfigNodeIdentityStatus
                    ),
                    nodeIdentityStates = nodeIdentityStates.toList()
                )
            } catch (ex: Exception) {
                _uiState.value = _uiState.value.copy(
                    messageState = Failed(message = message, error = ex),
                    isRefreshing = false,
                )
            }
        }
    }

    internal fun resetMessageState() {
        _uiState.value = _uiState.value.copy(messageState = NotStarted)
    }

    internal fun addNetworkKey() = repository.addNetworkKey()

    internal fun addApplicationKey() = repository.addApplicationKey(
        boundNetworkKey = meshNetwork.networkKeys.first()
    )

    fun save() {
        viewModelScope.launch {
            repository.save()
        }
    }
}

sealed interface NodeState {

    data object Loading : NodeState

    data class Success(
        val node: Node,
        val nodeInfoListData: NodeInfoListData,
    ) : NodeState

    data class Error(val throwable: Throwable) : NodeState
}

internal data class NodeScreenUiState(
    val nodeState: NodeState = NodeState.Loading,
    val isRefreshing: Boolean = false,
    val messageState: MessageState = NotStarted,
    val selectedNodeInfoItem: ClickableNodeInfoItem? = null,
    val nodeIdentityStates: List<NodeIdentityStatus> = emptyList(),
    val availableNetworkKeys: List<NetworkKey> = emptyList(),
    val availableAppKeys: List<ApplicationKey> = emptyList(),
)