package no.nordicsemi.android.nrfmesh.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.nrfmesh.core.common.Utils.toAndroidLogLevel
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import no.nordicsemi.android.nrfmesh.destinations.netKeySelector
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.logger.LogCategory
import no.nordicsemi.kotlin.mesh.logger.LogLevel
import no.nordicsemi.kotlin.mesh.logger.Logger
import javax.inject.Inject

@HiltViewModel
class NetKeySelectorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    navigator: Navigator,
    private val repository: DataStoreRepository
) : SimpleNavigationViewModel(navigator = navigator, savedStateHandle = savedStateHandle), Logger {

    private lateinit var meshNetwork: MeshNetwork

    private val selectedNetKey: KeyIndex = parameterOf(netKeySelector).toUShort()


    private var _uiState = MutableStateFlow(NetworkKeySelectionScreenUiState())
    internal val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.network.collect {
                meshNetwork = it
                _uiState.value = NetworkKeySelectionScreenUiState(
                    keys = meshNetwork.networkKeys,
                    selectedKeyIndex = selectedNetKey
                )
            }
        }
    }

    fun onKeySelected(keyIndex: KeyIndex) {
        _uiState.value = _uiState.value.copy(selectedKeyIndex = keyIndex)
    }

    override fun log(message: String, category: LogCategory, level: LogLevel) {
        Log.println(level.toAndroidLogLevel(), category.category, message)
    }
}

internal data class NetworkKeySelectionScreenUiState internal constructor(
    val keys: List<NetworkKey> = emptyList(),
    val selectedKeyIndex: KeyIndex = 0u,
)