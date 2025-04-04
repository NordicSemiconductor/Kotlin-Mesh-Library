package no.nordicsemi.android.nrfmesh.feature.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
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
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigResponse
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNodeIdentityStatus
import no.nordicsemi.kotlin.mesh.core.model.Element
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Model
import javax.inject.Inject

@HiltViewModel
internal class ModelViewModel @Inject internal constructor(
    private val repository: CoreDataRepository
) : ViewModel() {
    private lateinit var meshNetwork: MeshNetwork
    private lateinit var selectedElement: Element
    private lateinit var selectedModel: Model

    private val _uiState = MutableStateFlow(ModelScreenUiState())
    val uiState: StateFlow<ModelScreenUiState> = _uiState.asStateFlow()
    private lateinit var job: Job

    init {
        repository.network.onEach {
            meshNetwork = it

        }.launchIn(scope = viewModelScope)
    }

    /**
     * Sends a message to the node.
     *
     * @param message Message to be sent.
     */
    internal fun send(message: AcknowledgedConfigMessage) {
        _uiState.value =
            _uiState.value.copy(messageState = Sending(message = message))
        val handler = CoroutineExceptionHandler { _, throwable ->
            _uiState.value = _uiState.value.copy(
                messageState = Failed(message = message, error = throwable),
                isRefreshing = false
            )
        }
        job = viewModelScope.launch(context = handler) {
            repository.send(selectedElement.parentNode!!, message)?.let { response ->
                _uiState.value = _uiState.value.copy(
                    messageState = Completed(
                        message = message,
                        response = response as ConfigResponse
                    ),
                    isRefreshing = false,
                )

                if (response is ConfigNodeIdentityStatus) {
                    val nodeIdentityStates = _uiState.value.nodeIdentityStates.toMutableList()
                    val index = nodeIdentityStates.indexOfFirst { state ->
                        state.networkKey.index == response.index
                    }
                    nodeIdentityStates[index] = nodeIdentityStates[index]
                        .copy(nodeIdentityState = response.identity)
                    _uiState.value = _uiState.value.copy(
                        nodeIdentityStates = nodeIdentityStates.toList()
                    )
                }
            } ?: run {
                _uiState.value = _uiState.value.copy(
                    messageState = Failed(
                        message = message,
                        error = IllegalStateException("No response received")
                    ),
                    isRefreshing = false,
                )
            }
        }
    }
}

internal sealed interface ModelState {

    data object Loading : ModelState

    data class Success(val model: Model) : ModelState

    data class Error(val throwable: Throwable) : ModelState
}

internal data class ModelScreenUiState internal constructor(
    val modelState: ModelState = ModelState.Loading,
    val isRefreshing: Boolean = false,
    val messageState: MessageState = NotStarted,
    val nodeIdentityStates: List<NodeIdentityStatus> = emptyList()
)