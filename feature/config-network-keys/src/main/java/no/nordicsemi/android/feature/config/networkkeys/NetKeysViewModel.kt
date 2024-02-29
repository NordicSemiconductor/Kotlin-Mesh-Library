package no.nordicsemi.android.feature.config.networkkeys

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.feature.network.keys.destinations.networkKeys
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.Node
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NetKeysViewModel @Inject constructor(
    private val navigator: Navigator,
    savedStateHandle: SavedStateHandle,
    private val repository: CoreDataRepository
) : SimpleNavigationViewModel(navigator = navigator, savedStateHandle = savedStateHandle) {
    private lateinit var selectedNode: Node

    private val nodeUuid: UUID = parameterOf(configNetKeys)
    private lateinit var meshNetwork: MeshNetwork

    val uiState: StateFlow<NetKeysScreenUiState> = repository.network.onEach { network ->
        meshNetwork = network
    }.map {
        it.node(nodeUuid)?.let { node ->
            this@NetKeysViewModel.selectedNode = node
            NetKeysState.Success(netKeys = node.networkKeys)
        } ?: NetKeysState.Error(Throwable("Node not found"))
        NetKeysScreenUiState(
            netKeysState = NetKeysState.Success(netKeys = selectedNode.networkKeys),
            keys = it.networkKeys.filter { networkKey ->
                networkKey !in selectedNode.networkKeys
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NetKeysScreenUiState()
    )

    fun onSwiped(networkKey: NetworkKey) {
        TODO("Not yet implemented")
    }

    fun addNetworkKey(): NetworkKey {
        TODO("Not yet implemented")
    }

    internal fun navigateToNetworkKeys() {
        navigateTo(networkKeys)
    }
}

sealed interface NetKeysState {
    data class Success(
        val netKeys: List<NetworkKey>
    ) : NetKeysState

    data class Error(val throwable: Throwable) : NetKeysState
    data object Loading : NetKeysState
}

data class NetKeysScreenUiState internal constructor(
    val netKeysState: NetKeysState = NetKeysState.Loading,
    val keys: List<NetworkKey> = emptyList()
)

