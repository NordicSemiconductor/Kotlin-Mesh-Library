package no.nordicsemi.android.feature.config.networkkeys

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
import no.nordicsemi.kotlin.mesh.core.messages.StatusMessage
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNetKeyAdd
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNetKeyDelete
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.Node
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ConfigNetKeysViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CoreDataRepository
) : ViewModel() {
    private lateinit var selectedNode: Node

    private val nodeUuid: UUID =
        checkNotNull(savedStateHandle[MeshNavigationDestination.ARG]).let {
            UUID.fromString(it as String)
        }
    private lateinit var meshNetwork: MeshNetwork

    private val _uiState = MutableStateFlow(NetKeysScreenUiState())
    val uiState: StateFlow<NetKeysScreenUiState> = _uiState.asStateFlow()

    init {
        repository.network.onEach {
            meshNetwork = it
            val netKeysState = it.node(nodeUuid)?.let { node ->
                this@ConfigNetKeysViewModel.selectedNode = node
                NetKeysState.Success(netKeys = node.networkKeys)
            } ?: NetKeysState.Error(Throwable("Node not found"))
            _uiState.value = _uiState.value.copy(
                netKeysState = netKeysState,
                keys = it.networkKeys.filter { networkKey ->
                    networkKey !in selectedNode.networkKeys
                }
            )
        }.launchIn(scope = viewModelScope)
    }

    fun onSwiped(networkKey: NetworkKey) {
        send(message = ConfigNetKeyDelete(networkKey.index))
    }

    fun addNetworkKey(networkKey: NetworkKey) {
        send(message = ConfigNetKeyAdd(networkKey))
    }

    private fun send(message: AcknowledgedConfigMessage) {
        val handler = CoroutineExceptionHandler { _, throwable ->
            _uiState.value = _uiState.value.copy(
                messageState = Failed(message = message, error = throwable)
            )
        }
        _uiState.value = _uiState.value.copy(messageState = Sending(message = message))
        viewModelScope.launch(context = handler) {
            val msg = repository.send(selectedNode, message)
            msg.let { response ->
                _uiState.value = _uiState.value.copy(
                    messageState = Completed(
                        message = message,
                        response = response as StatusMessage
                    )
                )
            }
        }
    }

    fun resetMessageState() {
        _uiState.value = _uiState.value.copy(messageState = NotStarted)
    }
}

sealed interface NetKeysState {

    data object Loading : NetKeysState

    data class Success(
        val netKeys: List<NetworkKey>
    ) : NetKeysState

    data class Error(val throwable: Throwable) : NetKeysState
}

data class NetKeysScreenUiState internal constructor(
    val netKeysState: NetKeysState = NetKeysState.Loading,
    val keys: List<NetworkKey> = emptyList(),
    val showProgress: Boolean = false,
    val messageState: MessageState = NotStarted
)

