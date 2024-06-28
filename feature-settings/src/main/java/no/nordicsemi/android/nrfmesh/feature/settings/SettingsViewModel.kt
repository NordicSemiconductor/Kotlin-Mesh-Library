package no.nordicsemi.android.nrfmesh.feature.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    navigator: Navigator,
    private val repository: CoreDataRepository
) : SimpleNavigationViewModel(navigator, savedStateHandle) {

    private val _uiState = MutableStateFlow(SettingsScreenUiState())
    val uiState: StateFlow<SettingsScreenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.network.collect { network ->
                _uiState.update { state ->
                    when (val networkState = state.networkState) {
                        is MeshNetworkState.Loading -> SettingsScreenUiState(
                            networkState = MeshNetworkState.Success(
                                network = network
                            )
                        )

                        is MeshNetworkState.Success -> state.copy(
                            networkState = networkState.copy(
                                network = network
                            )
                        )

                        else -> state
                    }
                }
            }
        }
    }

    /**
     * Invoked when the name of the network is changed.
     *
     * @param name Name of the network.
     */
    internal fun onNameChanged(name: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                val networkState = state.networkState as MeshNetworkState.Success
                networkState.network.apply {
                    this.name = name
                }
                state.copy(networkState = networkState)
            }
        }
        save()
    }


    private fun save() {
        viewModelScope.launch { repository.save() }
    }
}

sealed interface MeshNetworkState {
    data class Success(val network: MeshNetwork) : MeshNetworkState
    data class Error(val throwable: Throwable) : MeshNetworkState
    object Loading : MeshNetworkState
}

data class SettingsScreenUiState(val networkState: MeshNetworkState = MeshNetworkState.Loading)