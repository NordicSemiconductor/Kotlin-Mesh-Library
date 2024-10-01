package no.nordicsemi.android.nrfmesh.feature.elements

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.core.common.NotStarted
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.kotlin.mesh.core.model.Address
import no.nordicsemi.kotlin.mesh.core.model.Element
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Node
import javax.inject.Inject

@HiltViewModel
internal class ElementViewModel @Inject internal constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CoreDataRepository
) : ViewModel() {
    private lateinit var meshNetwork: MeshNetwork
    private lateinit var selectedElement: Element
    private val address: Address = checkNotNull(
        value = savedStateHandle[MeshNavigationDestination.ARG]
    ).toString().toUShort(radix = 16)

    private val _uiState = MutableStateFlow(ElementScreenUiState())
    val uiState: StateFlow<ElementScreenUiState> = _uiState.asStateFlow()

    init {
        repository.network.onEach {
            meshNetwork = it
            val state = it.element(elementAddress = address)?.let { element ->
                this@ElementViewModel.selectedElement = element
                ElementState.Success(node = element.parentNode!!, element = selectedElement)
            } ?: ElementState.Error(Throwable("Node not found"))
            _uiState.value = _uiState.value.copy(
                elementState = state
            )
        }.launchIn(scope = viewModelScope)
    }

    /**
     * Called when the user changes the name of the node.
     *
     * @param name New name of the node.
     */
    internal fun onNameChanged(name: String) {
        if (selectedElement.name != name) {
            if (name.isNotEmpty())
                selectedElement.name = name
            else throw IllegalArgumentException("Name cannot be empty")
            viewModelScope.launch {
                repository.save()
            }
        }
    }
}

internal sealed interface ElementState {

    data object Loading : ElementState

    data class Success(val node: Node, val element: Element) : ElementState

    data class Error(val throwable: Throwable) : ElementState
}

internal data class ElementScreenUiState internal constructor(
    val elementState: ElementState = ElementState.Loading,
    val isRefreshing: Boolean = false,
    val showProgress: Boolean = false,
    val messageState: MessageState = NotStarted
)