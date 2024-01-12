package no.nordicsemi.android.nrfmesh.feature.application.keys

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.feature.application.keys.destinations.applicationKey
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import javax.inject.Inject

@HiltViewModel
internal class ApplicationKeyViewModel @Inject internal constructor(
    navigator: Navigator,
    savedStateHandle: SavedStateHandle,
    private val repository: CoreDataRepository
) : SimpleNavigationViewModel(navigator, savedStateHandle) {
    private val appKeyIndexArg: KeyIndex = parameterOf(applicationKey).toUShort()
    private lateinit var key: ApplicationKey


    val _uiState = MutableStateFlow(ApplicationKeyScreenUiState(ApplicationKeyState.Loading))
    val uiState: StateFlow<ApplicationKeyScreenUiState> = _uiState.asStateFlow()

    init {

        repository.network.onEach { meshNetwork ->
            _uiState.update { state ->
                val key = meshNetwork.applicationKey(appKeyIndexArg)
                when (val keyState = state.applicationKeyState) {
                    is ApplicationKeyState.Loading -> ApplicationKeyScreenUiState(
                        applicationKeyState = ApplicationKeyState.Success(
                            applicationKey = key,
                            networkKeys = meshNetwork.networkKeys.toList()
                        )
                    )
                    is ApplicationKeyState.Success -> state.copy(applicationKeyState = keyState.copy(
                        applicationKey = key
                    ))
                    else -> state
                }
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Invoked when the name of the application key is changed.
     *
     * @param name New application key name.
     */
    internal fun onNameChanged(name: String) {
        if (key.name != name) {
            key.name = name
            save()
        }
    }

    /**
     * Invoked when the application key is changed.
     *
     * @param key New application key.
     */
    internal fun onKeyChanged(key: ByteArray) {
        if (!this.key.key.contentEquals(key)) {
            this.key.setKey(key = key)
            save()
        }
    }

    /**
     * Invoked when the bound network key is changed.
     *
     * @param key New network key to bind to
     */
    internal fun onBoundNetworkKeyChanged(key: NetworkKey) {
        if (this.key.boundNetKeyIndex != key.index) {
            this.key.boundNetKeyIndex = key.index
            save()
        }
    }

    /**
     * Saves the network.
     */
    private fun save() {
        viewModelScope.launch { repository.save() }
    }
}

sealed interface ApplicationKeyState {
    data class Success(
        val applicationKey: ApplicationKey,
        val networkKeys: List<NetworkKey>
    ) : ApplicationKeyState

    data class Error(val throwable: Throwable) : ApplicationKeyState
    data object Loading : ApplicationKeyState
}

data class ApplicationKeyScreenUiState internal constructor(
    val applicationKeyState: ApplicationKeyState = ApplicationKeyState.Loading
)