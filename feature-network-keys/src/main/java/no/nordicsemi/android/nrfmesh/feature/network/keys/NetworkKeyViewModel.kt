package no.nordicsemi.android.nrfmesh.feature.network.keys

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.feature.network.keys.destinations.networkKey
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import javax.inject.Inject

@HiltViewModel
internal class NetworkKeyViewModel @Inject internal constructor(
    navigator: Navigator,
    savedStateHandle: SavedStateHandle,
    private val repository: CoreDataRepository
) : SimpleNavigationViewModel(navigator = navigator, savedStateHandle = savedStateHandle) {
    private lateinit var key: NetworkKey
    private val netKeyIndexArg: KeyIndex = parameterOf(networkKey).toUShort()

    val uiState: StateFlow<NetworkKeyScreenUiState> = repository.network.map { network ->
        this@NetworkKeyViewModel.key = network.networkKey(netKeyIndexArg)
        NetworkKeyScreenUiState(
            networkKeyState = NetworkKeyState.Success(
                networkKey = key
            )
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        NetworkKeyScreenUiState(NetworkKeyState.Loading)
    )

    init {
        save()
    }

    /**
     * Invoked when the name of the network key is changed.
     *
     * @param name New network key name.
     */
    internal fun onNameChanged(name: String) {
        if (key.name != name) {
            key.name = name
            save()
        }
    }

    /**
     * Invoked when the network key is changed.
     *
     * @param key New network key.
     */
    internal fun onKeyChanged(key: ByteArray) {
        if (!this.key.key.contentEquals(key)) {
            this.key.setKey(key = key)
            save()
        }
    }

    /**
     * Saves the network.
     */
    private fun save() {
        viewModelScope.launch { repository.save() }
    }
}

sealed interface NetworkKeyState {
    data class Success(val networkKey: NetworkKey) : NetworkKeyState
    data class Error(val throwable: Throwable) : NetworkKeyState
    data object Loading : NetworkKeyState
}

data class NetworkKeyScreenUiState internal constructor(
    val networkKeyState: NetworkKeyState = NetworkKeyState.Loading
)