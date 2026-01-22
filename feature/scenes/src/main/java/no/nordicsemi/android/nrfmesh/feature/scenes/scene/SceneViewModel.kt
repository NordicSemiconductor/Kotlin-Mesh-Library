package no.nordicsemi.android.nrfmesh.feature.scenes.scene

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
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.kotlin.data.HexString
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Scene

@HiltViewModel(assistedFactory = SceneViewModel.Factory::class)
internal class SceneViewModel @AssistedInject internal constructor(
    private val repository: CoreDataRepository,
    @Assisted number: HexString,
) : ViewModel() {
    private val sceneNumber = number.toUShort(radix = 16)
    private lateinit var network: MeshNetwork
    private val _uiState = MutableStateFlow(SceneScreenUiState())
    internal val uiState: StateFlow<SceneScreenUiState> = _uiState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SceneScreenUiState()
        )

    init {
        observeNetwork()
    }

    private fun observeNetwork() {
        repository.network.onEach { network ->
            this.network = network
            val sceneState = network.scene(number = sceneNumber)?.let { scene ->
                SceneState.Success(scene = scene)
            } ?: SceneState.Error(throwable = IllegalStateException("Scene not found."))
            _uiState.update { state ->
                state.copy(sceneState = sceneState)
            }
        }.launchIn(scope = viewModelScope)
    }

    /**
     * Saves the network.
     */
    internal fun save() {
        repository.save()
    }

    @AssistedFactory
    interface Factory {
        fun create(number: HexString): SceneViewModel
    }
}

internal sealed interface SceneState {

    data object Loading : SceneState

    data class Success(val scene: Scene) : SceneState

    data class Error(val throwable: Throwable) : SceneState
}

@ConsistentCopyVisibility
internal data class SceneScreenUiState internal constructor(
    val sceneState: SceneState = SceneState.Loading,
)
