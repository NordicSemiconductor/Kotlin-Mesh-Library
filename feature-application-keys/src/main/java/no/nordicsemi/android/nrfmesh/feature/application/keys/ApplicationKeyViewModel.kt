package no.nordicsemi.android.nrfmesh.feature.application.keys

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
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import no.nordicsemi.android.nrfmesh.feature.application.keys.destinations.applicationKey
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import javax.inject.Inject

@HiltViewModel
internal class ApplicationKeyViewModel @Inject internal constructor(
    navigator: Navigator,
    savedStateHandle: SavedStateHandle,
    private val repository: DataStoreRepository
) : SimpleNavigationViewModel(navigator, savedStateHandle) {
    private lateinit var key: ApplicationKey
    private val appKeyIndexArg: KeyIndex = parameterOf(applicationKey).toUShort()

    val uiState: StateFlow<ApplicationKeyScreenUiState> = repository.network.map { network ->
        this@ApplicationKeyViewModel.key =
            network.applicationKey(appKeyIndexArg)
        ApplicationKeyScreenUiState(
            applicationKeyState = ApplicationKeyState.Success(
                applicationKey = key,
                networkKeys = mutableListOf<NetworkKey>().apply {
                    addAll(network.networkKeys)
                }.toList()
            )
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ApplicationKeyScreenUiState(ApplicationKeyState.Loading)
    )

    override fun onCleared() {
        super.onCleared()
        save()
    }

    /**
     * Invoked when the name of the application key is changed.
     *
     * @param name New application key name.
     */
    internal fun onNameChanged(name: String) {
        if (key.name != name) {
            key.name = name
            save()
        }
    }

    /**
     * Invoked when the application key is changed.
     *
     * @param key New application key.
     */
    internal fun onKeyChanged(key: ByteArray) {
        if (!this.key.key.contentEquals(key)) {
            this.key.setKey(key = key)
            save()
        }
    }

    /**
     * Invoked when the bound network key is changed.
     *
     * @param key New network key to bind to
     */
    internal fun onBoundNetworkKeyChanged(key: NetworkKey) {
        if (this.key.boundNetKeyIndex != key.index) {
            this.key.boundNetKeyIndex = key.index
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

sealed interface ApplicationKeyState {
    data class Success(
        val applicationKey: ApplicationKey,
        val networkKeys: List<NetworkKey>
    ) : ApplicationKeyState

    data class Error(val throwable: Throwable) : ApplicationKeyState
    object Loading : ApplicationKeyState
}

@Suppress("ArrayInDataClass")
data class ApplicationKeyScreenUiState internal constructor(
    val applicationKeyState: ApplicationKeyState = ApplicationKeyState.Loading
)