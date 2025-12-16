package no.nordicsemi.android.nrfmesh.feature.proxy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
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
import no.nordicsemi.kotlin.mesh.core.messages.proxy.RemoveAddressesFromFilter
import no.nordicsemi.kotlin.mesh.core.model.Group
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.Node
import no.nordicsemi.kotlin.mesh.core.model.ProxyFilterAddress
import javax.inject.Inject

@HiltViewModel
internal class ProxyViewModel @Inject internal constructor(
    private val repository: CoreDataRepository,
) : ViewModel() {
    private var meshNetwork: MeshNetwork? = null
    private val _uiState = MutableStateFlow(ProxyScreenUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeNetwork()
    }

    private fun observeNetwork() {
        repository.network.onEach {
            meshNetwork = it
            _uiState.update {
                it.copy(
                    nodes = meshNetwork?.nodes?.toList() ?: emptyList(),
                    networkKeys = meshNetwork?.networkKeys?.toList() ?: emptyList(),
                    groups = meshNetwork?.groups?.toList() ?: emptyList()
                )
            }
        }.launchIn(scope = viewModelScope)

        repository.proxyConnectionStateFlow.onEach { proxyConnectionState ->
            _uiState.update {
                it.copy(proxyConnectionState = proxyConnectionState)
            }
        }.launchIn(scope = viewModelScope)

        // Setup initial state
        _uiState.update {
            it.copy(
                filterType = repository.proxyFilter.type,
                addresses = repository.proxyFilter.addresses.toList(),
            )
        }

        repository.proxyFilter.proxyFilterStateFlow.onEach { proxyFilterState ->
            println("ProxyViewModel: proxyFilterState: $proxyFilterState")
            when (proxyFilterState) {
                is ProxyFilterState.ProxyFilterUpdated -> {
                    val addresses = mutableListOf<ProxyFilterAddress>()
                    addresses.addAll(proxyFilterState.addresses)
                    _uiState.update {
                        it.copy(
                            filterType = proxyFilterState.type,
                            addresses = proxyFilterState.addresses.toList(),
                            isProxyLimitReached = false
                        )
                    }
                }

                is ProxyFilterState.ProxyFilterLimitReached -> {
                    _uiState.update {
                        it.copy(
                            filterType = proxyFilterState.type,
                            isProxyLimitReached = true
                        )
                    }
                }

                is ProxyFilterState.ProxyFilterUpdateAcknowledged -> {
                    _uiState.update {
                        it.copy(
                            filterType = proxyFilterState.type,
                            addresses = repository.proxyFilter.addresses.toList(),
                        )
                    }
                }

                ProxyFilterState.Unknown -> {

                }
            }
        }.launchIn(scope = viewModelScope)
    }

    internal fun onAutoConnectToggled(enabled: Boolean) {
        viewModelScope.launch {
            repository.enableAutoConnectProxy(meshNetwork = meshNetwork, enabled = enabled)
        }
    }

    internal fun connect(result: ScanResult) {
        viewModelScope.launch {
            with(repository) {
                disconnect()
                connectOverGattBearer(peripheral = result.peripheral)
            }
        }
    }

    internal fun disconnect() {
        viewModelScope.launch { repository.disconnect() }
    }

    internal fun onBluetoothEnabled(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled) {
                repository.startAutomaticConnectivity(meshNetwork = meshNetwork)
            }
        }
    }

    internal fun onLocationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled) {
                repository.startAutomaticConnectivity(meshNetwork = meshNetwork)
            }
        }
    }

    internal fun send(message: ProxyConfigurationMessage) {
        _uiState.update {
            it.copy(messageState = Sending(message = message))
        }
        viewModelScope.launch {
            try {
                repository.send(message).let { response ->
                    if (message is RemoveAddressesFromFilter) {
                        val addresses = _uiState.value.addresses.toMutableList()
                        if (addresses.removeAll(message.addresses)) {
                            _uiState.update {
                                it.copy(addresses = addresses.toList())
                            }
                        }
                    }
                    _uiState.update {
                        it.copy(
                            messageState = Completed(
                                message = message,
                                response = response as ConfigResponse
                            ),
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(messageState = Failed(message = message, error = e))
                }
            }
        }
    }

    internal fun resetMessageState() {
        _uiState.value = _uiState.value.copy(messageState = NotStarted, isProxyLimitReached = false)
    }
}

internal data class ProxyScreenUiState(
    val nodes: List<Node> = emptyList(),
    val networkKeys: List<NetworkKey> = emptyList(),
    val groups: List<Group> = emptyList(),
    val proxyConnectionState: ProxyConnectionState = ProxyConnectionState(),
    val addresses: List<ProxyFilterAddress> = emptyList(),
    val filterType: ProxyFilterType? = null,
    val isProxyLimitReached: Boolean = false,
    val messageState: MessageState = NotStarted,
)