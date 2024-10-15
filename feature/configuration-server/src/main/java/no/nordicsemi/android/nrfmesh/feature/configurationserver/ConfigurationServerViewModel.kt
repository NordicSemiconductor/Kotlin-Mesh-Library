package no.nordicsemi.android.nrfmesh.feature.configurationserver

import androidx.lifecycle.SavedStateHandle
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
import no.nordicsemi.android.nrfmesh.core.common.NotStarted
import no.nordicsemi.android.nrfmesh.core.common.Sending
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination.Companion.ARG
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigResponse
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNodeIdentityGet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNodeIdentityStatus
import no.nordicsemi.kotlin.mesh.core.model.Address
import no.nordicsemi.kotlin.mesh.core.model.Element
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.NodeIdentityState
import no.nordicsemi.kotlin.mesh.core.model.SigModelId
import no.nordicsemi.kotlin.mesh.core.model.model
import javax.inject.Inject

@HiltViewModel
internal class ConfigurationServerViewModel @Inject internal constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CoreDataRepository
) : ViewModel() {
    private lateinit var meshNetwork: MeshNetwork
    private lateinit var selectedElement: Element
    private lateinit var selectedModel: Model
    private val address: Address = checkNotNull(value = savedStateHandle[ARG])
        .toString()
        .toUShort(radix = 16)

    private val _uiState = MutableStateFlow(ModelScreenUiState())
    val uiState: StateFlow<ModelScreenUiState> = _uiState.asStateFlow()
    private lateinit var job: Job

    init {
        repository.network.onEach {
            meshNetwork = it
            val modelState = it.element(elementAddress = address)?.let { element ->
                this@ConfigurationServerViewModel.selectedElement = element
                selectedModel = element.models
                    .model(modelId = SigModelId(0x0000.toUShort()))
                    ?: throw IllegalArgumentException()
                ModelState.Success(model = selectedModel)
            } ?: ModelState.Error(Throwable("Model not found"))
            _uiState.value = _uiState.value.copy(
                modelState = modelState
            )
            if (shouldUpdateNodeIdentityState()) {
                _uiState.value = _uiState.value.copy(
                    nodeIdentityStates = createNodeIdentityStates()
                )
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
    private fun createNodeIdentityStates() = selectedModel.parentElement?.parentNode?.networkKeys
        ?.map { key ->
            NodeIdentityStatus(
                networkKey = key,
                nodeIdentityState = null
            )
        } ?: emptyList()

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
                        state.networkKey.index == response.networkKeyIndex
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

    internal fun requestNodeIdentityStates() {
        job = viewModelScope.launch {
            val uiState = _uiState.value
            val nodeIdentityStates = uiState.nodeIdentityStates.toMutableList()
            val keys = selectedElement.parentNode?.networkKeys ?: emptyList()

            var message: ConfigNodeIdentityGet? = null
            var response: ConfigNodeIdentityStatus? = null
            try {
                keys.forEach { key ->
                    message = ConfigNodeIdentityGet(networkKeyIndex = key.index)
                    _uiState.value = _uiState.value.copy(
                        messageState = Sending(message = message!!),
                    )
                    response = repository.send(
                        node = selectedElement.parentNode!!,
                        message = message!!
                    ) as ConfigNodeIdentityStatus

                    response?.let { status ->
                        val index = nodeIdentityStates.indexOfFirst { state ->
                            state.networkKey.index == status.networkKeyIndex
                        }
                        nodeIdentityStates[index] = nodeIdentityStates[index]
                            .copy(nodeIdentityState = status.identity)
                    }
                }
                _uiState.value = _uiState.value.copy(
                    messageState = Completed(
                        message = ConfigNodeIdentityGet(networkKeyIndex = keys.first().index),
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

    private fun cancel() {
        job.cancel()
    }

    fun resetMessageState() {
        _uiState.value = _uiState.value.copy(messageState = NotStarted)
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

internal data class NodeIdentityStatus(
    val networkKey: NetworkKey,
    val nodeIdentityState: NodeIdentityState?
)