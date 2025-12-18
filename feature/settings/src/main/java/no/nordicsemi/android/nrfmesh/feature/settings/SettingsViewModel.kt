package no.nordicsemi.android.nrfmesh.feature.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.core.data.storage.MeshSecurePropertiesStorage
import no.nordicsemi.android.nrfmesh.core.navigation.ClickableSetting
import no.nordicsemi.android.nrfmesh.feature.settings.navigation.SettingsRoute
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: CoreDataRepository,
    private val storage: MeshSecurePropertiesStorage,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsScreenUiState())
    val uiState: StateFlow<SettingsScreenUiState> = _uiState.asStateFlow()
    private lateinit var network: MeshNetwork

    init {
        _uiState.update {
            it.copy(selectedSetting = savedStateHandle.toRoute<SettingsRoute>().selectedSetting)
        }
        observeNetworkState()
    }

    /**
     * Observes the network state and updates the UI state with the current network data.
     */
    private fun observeNetworkState() {
        repository.network.onEach {
            _uiState.update { state ->
                state.copy(
                    networkState = MeshNetworkState.Success(
                        network = it,
                        settingsListData = SettingsListData(it)
                    ),
                    selectedSetting = state.selectedSetting
                )
            }
            network = it
        }.launchIn(scope = viewModelScope)
    }

    /**
     * Invoked when a setting is selected.
     *
     * @param clickableSetting The setting that was clicked.
     */
    internal fun onItemSelected(clickableSetting: ClickableSetting) {
        _uiState.update { it.copy(selectedSetting = clickableSetting) }
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

    /**
     * Moves the provisioner to a new index in the list.
     */
    @OptIn(ExperimentalUuidApi::class)
    fun moveProvisioner(provisioner: Provisioner, newIndex: Int) {
        viewModelScope.launch {
            network.move(provisioner = provisioner, to = newIndex)
            storage.storeLocalProvisioner(
                uuid = network.uuid,
                localProvisionerUuid = provisioner.uuid
            )
            repository.save()
        }
    }

    fun save() {
        repository.save()
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