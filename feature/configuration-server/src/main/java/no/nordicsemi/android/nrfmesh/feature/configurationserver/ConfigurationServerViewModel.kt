package no.nordicsemi.android.nrfmesh.feature.configurationserver

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
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination.Companion.ARG
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigResponse
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigGattProxyGet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigGattProxySet
import no.nordicsemi.kotlin.mesh.core.model.Address
import no.nordicsemi.kotlin.mesh.core.model.Element
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Model
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
    private val address: Address = checkNotNull(value = savedStateHandle[ARG]).toString().toUShort()

    private val _uiState = MutableStateFlow(ModelScreenUiState())
    val uiState: StateFlow<ModelScreenUiState> = _uiState.asStateFlow()

    init {
        repository.network.onEach {
            meshNetwork = it
            val state = it.node(address = address)?.let { node ->
                this@ConfigurationServerViewModel.selectedElement =
                    node.element(address) ?: throw IllegalArgumentException()
                selectedModel = selectedElement.models
                    .model(modelId = SigModelId(0x0000.toUShort()))
                    ?: throw IllegalArgumentException()
                ModelState.Success(model = selectedModel)
            } ?: ModelState.Error(Throwable("Model not found"))
            _uiState.value = _uiState.value.copy(
                modelState = state
            )
        }.launchIn(scope = viewModelScope)
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
     * Called when the user requests the current friend feature state of the node.
     */
    internal fun onGetFriendStateClicked() {
        viewModelScope.launch {
            send(message = ConfigGattProxyGet())
        }
    }

    /**
     * Called when the user toggles the friend feature state of the node.
     *
     * @param enabled True if friend feature state is to be enabled or false otherwise.
     */
    internal fun onFriendStateToggled(enabled: Boolean) {
        viewModelScope.launch {
            send(message = ConfigGattProxySet(enabled))
        }
    }

    /**
     * Sends a message to the node.
     *
     * @param message Message to be sent.
     */
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
            repository.send(selectedElement.parentNode!!, message)?.let { response ->
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

internal sealed interface ModelState {

    data object Loading : ModelState

    data class Success(val model: Model) : ModelState

    data class Error(val throwable: Throwable) : ModelState
}

internal data class ModelScreenUiState internal constructor(
    val modelState: ModelState = ModelState.Loading,
    val isRefreshing: Boolean = false,
    val showProgress: Boolean = false,
    val messageState: MessageState = NotStarted
)