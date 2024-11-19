package no.nordicsemi.android.nrfmesh.feature.bind.appkeys

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
import no.nordicsemi.android.nrfmesh.feature.bind.appkeys.navigation.BoundAppKeysDestination.MODEL_ID
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigResponse
import no.nordicsemi.kotlin.mesh.core.model.Address
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.Element
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.ModelId
import no.nordicsemi.kotlin.mesh.core.model.ModelId.Companion.decode
import no.nordicsemi.kotlin.mesh.core.model.model
import javax.inject.Inject

@HiltViewModel
internal class BindAppKeysViewModel @Inject internal constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CoreDataRepository
) : ViewModel() {
    private lateinit var meshNetwork: MeshNetwork
    private lateinit var selectedElement: Element
    private lateinit var selectedModel: Model
    private val address: Address = checkNotNull(value = savedStateHandle[ARG])
        .toString()
        .toUShort(radix = 16)
    private val modelId: ModelId = checkNotNull(value = savedStateHandle[MODEL_ID])
        .toString()
        .decode()

    private val _uiState = MutableStateFlow(BindAppKeysScreenUiState())
    val uiState: StateFlow<BindAppKeysScreenUiState> = _uiState.asStateFlow()

    init {
        repository.network.onEach {
            meshNetwork = it
            val modelState = it.element(elementAddress = address)?.let { element ->
                this@BindAppKeysViewModel.selectedElement = element
                selectedModel = element.models
                    .model(modelId = modelId)
                    ?: throw IllegalArgumentException()
                ModelState.Success(model = selectedModel)
            } ?: ModelState.Error(Throwable("Model not found"))
            _uiState.value = _uiState.value.copy(
                modelState = modelState,
                boundKeys = selectedModel.boundApplicationKeys,
                addedKeys = selectedModel.parentElement?.parentNode?.applicationKeys?.filter { key ->
                    key !in selectedModel.boundApplicationKeys
                }.orEmpty()
            )
        }.launchIn(scope = viewModelScope)
    }

    internal fun send(message: AcknowledgedConfigMessage) {
        _uiState.value =
            _uiState.value.copy(messageState = Sending(message = message))
        val handler = CoroutineExceptionHandler { _, throwable ->
            _uiState.value = _uiState.value.copy(
                messageState = Failed(message = message, error = throwable),
                isRefreshing = false
            )
        }
        viewModelScope.launch(context = handler) {
            repository.send(selectedElement.parentNode!!, message)?.let { response ->
                _uiState.value = _uiState.value.copy(
                    messageState = Completed(
                        message = message,
                        response = response as ConfigResponse
                    ),
                    isRefreshing = false,
                )
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

internal data class BindAppKeysScreenUiState internal constructor(
    val modelState: ModelState = ModelState.Loading,
    val isRefreshing: Boolean = false,
    val messageState: MessageState = NotStarted,
    val boundKeys: List<ApplicationKey> = emptyList(),
    val addedKeys: List<ApplicationKey> = emptyList()
)