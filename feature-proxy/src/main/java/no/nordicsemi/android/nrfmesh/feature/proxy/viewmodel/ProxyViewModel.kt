package no.nordicsemi.android.nrfmesh.feature.proxy.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResults
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.core.data.ProxyState
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import javax.inject.Inject

@HiltViewModel
internal class ProxyViewModel @Inject internal constructor(
    navigator: Navigator,
    savedStateHandle: SavedStateHandle,
    private val repository: CoreDataRepository
) : SimpleNavigationViewModel(navigator, savedStateHandle) {
    private var meshNetwork: MeshNetwork? = null
    val uiState: StateFlow<ProxyScreenUiState> = repository.proxyStateFlow.transform {
        emit(ProxyScreenUiState(it))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProxyScreenUiState()
    )

    init {
        observeNetwork()
    }

    private fun observeNetwork() {
        repository.network.onEach {
            meshNetwork = it
        }.launchIn(viewModelScope)
    }

    internal fun onAutoConnectToggled(enabled: Boolean) {
        viewModelScope.launch {
            repository.enableAutoConnectProxy(meshNetwork = meshNetwork, enabled = enabled)
        }
    }

    internal fun connect(context: Context, results: BleScanResults) {
        viewModelScope.launch {
            repository.disconnect()
            repository.connectOverGattBearer(
                context = context,
                device = results.device
            )
        }
    }

    internal fun disconnect() {
        viewModelScope.launch {
            repository.disconnect()
        }
    }

    internal fun onBluetoothEnabled(enabled: Boolean) {
        viewModelScope.launch {
            with(repository) {
                onBluetoothEnabled(enabled)
                startAutomaticConnectivity(meshNetwork)
            }
        }
    }

    internal fun onLocationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            with(repository) {
                onLocationEnabled(enabled)
                startAutomaticConnectivity(meshNetwork)
            }
        }
    }
}

internal data class ProxyScreenUiState internal constructor(
    val proxyState: ProxyState = ProxyState()
)