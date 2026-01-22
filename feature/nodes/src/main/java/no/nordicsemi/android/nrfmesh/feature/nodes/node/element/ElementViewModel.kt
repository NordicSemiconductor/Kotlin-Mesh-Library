package no.nordicsemi.android.nrfmesh.feature.nodes.node.element

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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
import no.nordicsemi.android.nrfmesh.feature.nodes.node.NodeInfoListData
import no.nordicsemi.android.nrfmesh.feature.nodes.node.NodeState
import no.nordicsemi.kotlin.data.HexString
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigResponse
import no.nordicsemi.kotlin.mesh.core.messages.MeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.MeshResponse
import no.nordicsemi.kotlin.mesh.core.messages.UnacknowledgedMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigAppKeyGet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNodeIdentityGet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNodeIdentityStatus
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.Element
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.Node
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@HiltViewModel(assistedFactory = ElementViewModel.Factory::class)
internal class ElementViewModel @AssistedInject internal constructor(
    private val repository: CoreDataRepository,
    @Assisted address: HexString,
) : ViewModel() {
    private lateinit var meshNetwork: MeshNetwork
    private lateinit var selectedNode: Node
    private val address = address.toUShort(radix = 16)

    private val _uiState = MutableStateFlow(ElementScreenUiState())
    val uiState: StateFlow<ElementScreenUiState> = _uiState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ElementScreenUiState()
        )

    init {
        observeNetworkChanges()
        observeConfigNodeReset()
    }

    private fun observeNetworkChanges() {
        repository.network.onEach {
            val elementState = it.element(elementAddress = address)?.let { element ->
                selectedNode = element.parentNode!!
                ElementState.Success(element = element)
            } ?: ElementState.Error(Throwable("Element containing node not found"))
            _uiState.update { state ->
                state.copy(
                    elementState = elementState
                )
            }
            meshNetwork = it // update the local network instance
        }.launchIn(scope = viewModelScope)
    }

    /**
     * Observes incoming messages from the repository to handle node reset events.
     */
    private fun observeConfigNodeReset() {
        repository.incomingMessages.onEach {

        }.launchIn(scope = viewModelScope)
    }

    fun save() {
        viewModelScope.launch {
            repository.save()
        }
    }

    internal fun resetMessageState() {
        _uiState.value = _uiState.value.copy(messageState = NotStarted)
    }

    @AssistedFactory
    interface Factory {
        fun create(address: HexString): ElementViewModel
    }
}

internal sealed interface ElementState {

    data object Loading : ElementState

    data class Success(val element: Element) : ElementState

    data class Error(val throwable: Throwable) : ElementState
}

internal data class ElementScreenUiState(
    val elementState: ElementState = ElementState.Loading,
    val messageState: MessageState = NotStarted,
)