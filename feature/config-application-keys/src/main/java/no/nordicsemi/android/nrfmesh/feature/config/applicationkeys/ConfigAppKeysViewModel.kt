package no.nordicsemi.android.nrfmesh.feature.config.applicationkeys

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
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigAppKeyAdd
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigAppKeyDelete
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigAppKeyGet
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Node
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ConfigAppKeysViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CoreDataRepository
) : ViewModel() {
    private lateinit var selectedNode: Node

    private val nodeUuid: UUID =
        checkNotNull(savedStateHandle[MeshNavigationDestination.ARG]).let {
            UUID.fromString(it as String)
        }
    private lateinit var meshNetwork: MeshNetwork

    private val _uiState = MutableStateFlow(AppKeysScreenUiState())
    val uiState: StateFlow<AppKeysScreenUiState> = _uiState.asStateFlow()

    init {
        repository.network.onEach {
            meshNetwork = it
            val appKeysState = it.node(nodeUuid)?.let { node ->
                this@ConfigAppKeysViewModel.selectedNode = node
                AppKeysState.Success(appKeys = node.applicationKeys)
            } ?: AppKeysState.Error(Throwable("Node not found"))
            _uiState.value = _uiState.value.copy(
                appKeysState = appKeysState,
                keys = it.applicationKeys.filter { applicationKey ->
                    applicationKey !in selectedNode.applicationKeys
                }
            )
        }.launchIn(scope = viewModelScope)
    }

    internal fun onSwiped(key: ApplicationKey) {
        send(message = ConfigAppKeyDelete(key))
    }

    internal fun addApplicationKey(key: ApplicationKey) {
        send(message = ConfigAppKeyAdd(applicationKey = key))
    }

    /**
     * Called when the user pulls down to refresh the node details.
     */
    internal fun onRefresh() {
        _uiState.value = uiState.value.copy(isRefreshing = true)
        send(message = ConfigAppKeyGet(networkKey = meshNetwork.networkKeys.first()))
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

    fun resetMessageState() {
        _uiState.value = _uiState.value.copy(messageState = NotStarted)
    }
}

sealed interface AppKeysState {

    data object Loading : AppKeysState

    data class Success(
        val appKeys: List<ApplicationKey>
    ) : AppKeysState

    data class Error(val throwable: Throwable) : AppKeysState
}

data class AppKeysScreenUiState internal constructor(
    val appKeysState: AppKeysState = AppKeysState.Loading,
    val keys: List<ApplicationKey> = emptyList(),
    val showProgress: Boolean = false,
    val isRefreshing: Boolean = false,
    val messageState: MessageState = NotStarted
)

