package no.nordicsemi.android.nrfmesh.feature.application.keys.key

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
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey

@HiltViewModel(assistedFactory = ApplicationKeyViewModel.Factory::class)
internal class ApplicationKeyViewModel @AssistedInject internal constructor(
    private val repository: CoreDataRepository,
    @Assisted private val index: HexString,
) : ViewModel() {
    private val keyIndex = index.toUShort(radix = 16)
    private lateinit var network: MeshNetwork

    private val _uiState = MutableStateFlow(ApplicationKeyScreenUiState())
    internal val uiState: StateFlow<ApplicationKeyScreenUiState> = _uiState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ApplicationKeyScreenUiState()
        )

    init {
        observeNetwork()
    }

    private fun observeNetwork() {
        repository.network.onEach { network ->
            this.network = network
            val keyState = network.applicationKey(index = keyIndex)?.let { key ->
                AppKeyState.Success(key = key)
            } ?: AppKeyState.Error(throwable = IllegalStateException("Application Key not found."))
            _uiState.update { state ->
                state.copy(keyState = keyState)
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
        fun create(index: HexString): ApplicationKeyViewModel
    }
}

internal sealed interface AppKeyState {

    data object Loading : AppKeyState

    data class Success(val key: ApplicationKey) : AppKeyState

    data class Error(val throwable: Throwable) : AppKeyState
}

@ConsistentCopyVisibility
internal data class ApplicationKeyScreenUiState internal constructor(
    val keyState: AppKeyState = AppKeyState.Loading,
    val networkKeys: List<NetworkKey> = emptyList()
)
