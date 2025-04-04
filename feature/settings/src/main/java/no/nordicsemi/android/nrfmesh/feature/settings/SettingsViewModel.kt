package no.nordicsemi.android.nrfmesh.feature.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.core.navigation.ClickableSetting
import no.nordicsemi.android.nrfmesh.feature.settings.navigation.SettingsRoute
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: CoreDataRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsScreenUiState())
    val uiState: StateFlow<SettingsScreenUiState> = _uiState.asStateFlow()
    private lateinit var network: MeshNetwork

    init {
        _uiState.value = _uiState.value.copy(
            selectedSetting = savedStateHandle.toRoute<SettingsRoute>().selectedSetting
        )

        viewModelScope.launch {
            repository.network.collect {
                val selectedSetting = _uiState.value.selectedSetting
                _uiState.value = _uiState.value.copy(
                    networkState = MeshNetworkState.Success(
                        network = it,
                        settingsListData = SettingsListData(it)
                    ),
                    selectedSetting = selectedSetting
                )
                network = it
            }
        }
    }

    internal fun onItemSelected(clickableSetting: ClickableSetting) {
        _uiState.value = _uiState.value.copy(selectedSetting = clickableSetting)
    }

    /**
     * Invoked when the name of the network is changed.
     *
     * @param name Name of the network.
     */
    fun onNameChanged(name: String) {
        network.name = name
        save()
    }

    fun save() {
        viewModelScope.launch { repository.save() }
    }
}

sealed interface MeshNetworkState {
    data class Success(
        val network: MeshNetwork,
        val settingsListData: SettingsListData,
    ) : MeshNetworkState

    data object Loading : MeshNetworkState
}

data class SettingsScreenUiState(
    val networkState: MeshNetworkState = MeshNetworkState.Loading,
    val selectedSetting: ClickableSetting? = null,
)