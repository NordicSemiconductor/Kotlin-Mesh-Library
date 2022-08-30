package no.nordicsemi.android.feature.application.keys

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.nordicsemi.android.feature.application.keys.navigation.ApplicationKeyDestination
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import javax.inject.Inject

@HiltViewModel
class ApplicationKeyViewModel @Inject internal constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: DataStoreRepository
) : ViewModel() {
    private lateinit var applicationKey: ApplicationKey
    private val appKeyIndexArg: String =
        checkNotNull(savedStateHandle[ApplicationKeyDestination.appKeyIndexArg])

    val uiState: StateFlow<ApplicationKeyScreenUiState> = repository.network.map { network ->
        this@ApplicationKeyViewModel.applicationKey =
            network.applicationKey(appKeyIndexArg.toUShort())
        ApplicationKeyScreenUiState(
            applicationKeyState = ApplicationKeyState.Success(applicationKey = applicationKey)
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ApplicationKeyScreenUiState(ApplicationKeyState.Loading)
    )

    /**
     * Invoked when the name of the application key is changed.
     *
     * @param name New application key name.
     */
    internal fun onNameChanged(name: String) {
        if (applicationKey.name != name) {
            applicationKey.name = name
            save()
        }
    }

    /**
     * Invoked when the application key is changed.
     *
     * @param key New application key.
     */
    internal fun onKeyChanged(key: ByteArray) {
        if (!applicationKey.key.contentEquals(key)) {
            applicationKey.setKey(key = key)
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
    data class Success(val applicationKey: ApplicationKey) : ApplicationKeyState
    data class Error(val throwable: Throwable) : ApplicationKeyState
    object Loading : ApplicationKeyState
}

@Suppress("ArrayInDataClass")
data class ApplicationKeyScreenUiState internal constructor(
    val applicationKeyState: ApplicationKeyState = ApplicationKeyState.Loading
)