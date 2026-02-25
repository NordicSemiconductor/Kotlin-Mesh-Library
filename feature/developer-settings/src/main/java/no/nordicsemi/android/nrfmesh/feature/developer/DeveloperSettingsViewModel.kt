package no.nordicsemi.android.nrfmesh.feature.developer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.core.data.DeveloperSettings
import javax.inject.Inject

@HiltViewModel
class DeveloperSettingsViewModel @Inject constructor(
    private val repository: CoreDataRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(value = DeveloperSettingsScreenUi())
    internal val uiState = _uiState.asStateFlow()

    init {
        observeDeveloperSettingsChanges()
    }

    private fun observeDeveloperSettingsChanges() {
        repository.developerSettingsStateFlow.onEach {
            _uiState.update { state ->
                state.copy(settings = it)
            }
        }.launchIn(scope = viewModelScope)
    }
}

internal data class DeveloperSettingsScreenUi(
    val settings: DeveloperSettings = DeveloperSettings(),
)