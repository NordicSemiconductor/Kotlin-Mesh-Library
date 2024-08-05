package no.nordicsemi.android.feature.config.networkkeys

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.nrfmesh.core.common.Completed
import no.nordicsemi.android.nrfmesh.core.common.Failed
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.core.common.NotStarted
import no.nordicsemi.android.nrfmesh.core.common.Sending
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.feature.network.keys.destinations.networkKeys
import no.nordicsemi.android.nrfmesh.feature.settings.destinations.settings
import no.nordicsemi.kotlin.mesh.core.messages.MeshResponse
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNetKeyAdd
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.Node
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ConfigNetKeysViewModel @Inject constructor(
    private val navigator: Navigator,
    savedStateHandle: SavedStateHandle,
    private val repository: CoreDataRepository
) : SimpleNavigationViewModel(navigator = navigator, savedStateHandle = savedStateHandle) {
    private lateinit var selectedNode: Node

    private val nodeUuid: UUID = parameterOf(configNetKeys)
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
        TODO("Not yet implemented")
    }

    fun addNetworkKey(networkKey: NetworkKey) {

        val message = ConfigNetKeyAdd(networkKey)
        val handler = CoroutineExceptionHandler { _, throwable ->
            _uiState.value = _uiState.value.copy(
                messageState = Failed(message = message, error = throwable)
            )
        }
        _uiState.value = _uiState.value.copy(messageState = Sending(message = message))
        viewModelScope.launch(context = handler) {
            repository.send(selectedNode, message)?.let { response ->
                _uiState.value = _uiState.value.copy(
                    messageState = Completed(message = message, response = response as MeshResponse)
                )
            }
        }
    }

    fun resetMessageState(){
        _uiState.value = _uiState.value.copy(messageState = NotStarted)
    }

    internal fun navigateToNetworkKeys() {
        navigateTo(settings)
        navigateTo(networkKeys)
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

