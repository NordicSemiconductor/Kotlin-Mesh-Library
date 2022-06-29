package no.nordicsemi.android.nrfmesh.feature.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import no.nordicsemi.kotlin.mesh.core.model.Scene
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: DataStoreRepository
) : ViewModel() {

    var uiState by mutableStateOf(SettingsUiState())

    init {
        viewModelScope.launch {
            repository.network.collect {
                uiState = SettingsUiState(
                    networkName = it.name,
                    provisioners = it.provisioners,
                    networkKeys = it.networkKeys,
                    applicationKeys = it.applicationKeys,
                    scenes = it.scenes,
                    lastModified = it.timestamp
                )
            }
        }
    }
}

data class SettingsUiState(
    val networkName: String = "nRF Mesh",
    val provisioners: List<Provisioner> = emptyList(),
    val networkKeys: List<NetworkKey> = emptyList(),
    val applicationKeys: List<ApplicationKey> = emptyList(),
    val scenes: List<Scene> = emptyList(),
    val lastModified: Instant = Instant.fromEpochMilliseconds(System.currentTimeMillis())
)