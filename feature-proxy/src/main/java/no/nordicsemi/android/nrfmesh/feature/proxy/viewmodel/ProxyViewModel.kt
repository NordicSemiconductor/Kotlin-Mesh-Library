package no.nordicsemi.android.nrfmesh.feature.proxy.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResults
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.feature.proxy.viewmodel.ProxyState.*
import no.nordicsemi.kotlin.mesh.bearer.BearerEvent
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import javax.inject.Inject

@HiltViewModel
internal class ProxyViewModel @Inject internal constructor(
    navigator: Navigator,
    savedStateHandle: SavedStateHandle,
    private val repository: CoreDataRepository
) : SimpleNavigationViewModel(navigator, savedStateHandle) {
    private lateinit var meshNetwork: MeshNetwork

    private val _uiState = MutableStateFlow(
        ProxyScreenUiState(
            autoConnect = true,
            proxyState = Scanning
        )
    )
    val uiState = _uiState.asStateFlow()

    internal fun onAutomaticConnectionChanged(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(autoConnect = enabled)
    }

    internal fun connect(context: Context, results: BleScanResults) {
        viewModelScope.launch {
            val device = results.device
            _uiState.value = _uiState.value.copy(proxyState = Connecting(device = device))
            val gattBearer = repository.connectOverGattBearer(
                context = context,
                device = results.device
            )
            // gattBearer.open()
            gattBearer.state.takeWhile {
                it !is BearerEvent.Closed
            }.onEach {
                if (it is BearerEvent.Opened) {
                    _uiState.value = _uiState.value.copy(proxyState = Connected(device = device))
                }
            }.onCompletion {


            }.launchIn(this)
        }
    }

    internal fun disconnect() {
        viewModelScope.launch {
            repository.disconnect()
            _uiState.value = _uiState.value.copy(proxyState = Disconnected)
        }
    }
}

internal sealed class ProxyState {
    data object Scanning : ProxyState()
    data class Connecting(val device: ServerDevice) : ProxyState()
    data class Connected(val device: ServerDevice) : ProxyState()
    data object Disconnected : ProxyState()
}

internal data class ProxyScreenUiState internal constructor(
    val autoConnect: Boolean = true,
    val proxyState: ProxyState
)