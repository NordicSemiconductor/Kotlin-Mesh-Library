package no.nordicsemi.android.nrfmesh.feature.proxy

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.common.Completed
import no.nordicsemi.android.nrfmesh.core.common.Failed
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.core.common.NotStarted
import no.nordicsemi.android.nrfmesh.core.common.Sending
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.core.data.ProxyConnectionState
import no.nordicsemi.kotlin.ble.client.android.ScanResult
import no.nordicsemi.kotlin.mesh.core.ProxyFilterState
import no.nordicsemi.kotlin.mesh.core.ProxyFilterType
import no.nordicsemi.kotlin.mesh.core.messages.ConfigResponse
import no.nordicsemi.kotlin.mesh.core.messages.proxy.ProxyConfigurationMessage
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.ProxyFilterAddress
import javax.inject.Inject

@HiltViewModel
internal class ProxyViewModel @Inject internal constructor(
    private val repository: CoreDataRepository,
) : ViewModel() {
    private var meshNetwork: MeshNetwork? = null
    private val _uiState = MutableStateFlow<ProxyScreenUiState>(ProxyScreenUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeNetwork()
    }

    private fun observeNetwork() {
        repository.network.onEach {
            meshNetwork = it
            _uiState.value = _uiState.value.copy(network = it)
        }.launchIn(scope = viewModelScope)

        repository.proxyConnectionStateFlow.onEach {
            _uiState.value = _uiState.value.copy(proxyConnectionState = it)
        }.launchIn(scope = viewModelScope)

        repository.proxyFilter.proxyFilterStateFlow.onEach {
            when (it) {
                is ProxyFilterState.ProxyFilterUpdated -> {
                    val addresses = mutableListOf<ProxyFilterAddress>()
                    addresses.addAll(it.addresses)
                    _uiState.value = _uiState.value.copy(
                        filterType = it.type,
                        addresses = addresses.toList(),
                        isProxyLimitReached = false
                    )
                }

                is ProxyFilterState.ProxyFilterLimitReached ->
                    _uiState.value = _uiState.value.copy(
                        filterType = it.type,
                        isProxyLimitReached = true
                    )
                else -> {

                }
            }
        }.launchIn(scope = viewModelScope)
    }

    internal fun onAutoConnectToggled(enabled: Boolean) {
        viewModelScope.launch {
            repository.enableAutoConnectProxy(meshNetwork = meshNetwork, enabled = enabled)
        }
    }

    internal fun connect(context: Context, result: ScanResult) {
        viewModelScope.launch {
            repository.run {
                disconnect()
                connectOverGattBearer(context = context, peripheral = result.peripheral)
            }
        }
    }

    internal fun disconnect() {
        viewModelScope.launch { repository.disconnect() }
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

    internal fun send(message: ProxyConfigurationMessage) {
        _uiState.value = _uiState.value.copy(messageState = Sending(message = message))
        viewModelScope.launch {
            try {
                repository.send(message)?.let { response ->
                    _uiState.value = _uiState.value.copy(
                        messageState = Completed(
                            message = message,
                            response = response as ConfigResponse
                        ),
                    )
                } ?: run {
                    _uiState.value = _uiState.value.copy(
                        messageState = Failed(
                            message = message,
                            error = IllegalStateException("No response received")
                        ),
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    messageState = Failed(message = message, error = e)
                )
            }
        }
    }

    internal fun resetMessageState() {
        _uiState.value = _uiState.value.copy(messageState = NotStarted, isProxyLimitReached = false)
    }
}

internal data class ProxyScreenUiState(
    val network: MeshNetwork? = null,
    val proxyConnectionState: ProxyConnectionState = ProxyConnectionState(),
    val addresses: List<ProxyFilterAddress> = emptyList(),
    val filterType: ProxyFilterType? = null,
    val isProxyLimitReached: Boolean = false,
    val messageState: MessageState = NotStarted,
)