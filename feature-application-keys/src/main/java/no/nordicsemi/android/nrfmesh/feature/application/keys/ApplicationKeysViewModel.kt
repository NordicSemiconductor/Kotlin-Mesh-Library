package no.nordicsemi.android.nrfmesh.feature.application.keys

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.DestinationId
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import javax.inject.Inject

@HiltViewModel
internal class ApplicationKeysViewModel @Inject internal constructor(
    savedStateHandle: SavedStateHandle,
    private val navigator: Navigator,
    private val repository: CoreDataRepository
) : SimpleNavigationViewModel(navigator, savedStateHandle = savedStateHandle) {

    private lateinit var network: MeshNetwork

    private val _uiState = MutableStateFlow(ApplicationKeysScreenUiState(listOf()))
    val uiState: StateFlow<ApplicationKeysScreenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.network.collect { network ->
                this@ApplicationKeysViewModel.network = network
                _uiState.update { state ->
                    val keys = network.applicationKeys.toList()
                    state.copy(
                        keys = keys,
                        keysToBeRemoved = keys.filter { it in state.keysToBeRemoved }
                    )
                }
            }
        }
    }

    override fun onCleared() {
        removeKeys()
        super.onCleared()
    }

    internal fun navigate(destinationId: DestinationId<Int, Unit>, keyIndex: Int) {
        navigator.navigateTo(destinationId, keyIndex)
    }

    /**
     * Adds an application key to the network.
     */
    internal fun addApplicationKey(): ApplicationKey = network.add(
        name = "nRF Application Key",
        boundNetworkKey = network.networkKeys.first()
    )

    fun onSwiped(key: ApplicationKey) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(keysToBeRemoved = it.keysToBeRemoved + key)
            }
        }
    }

    fun onUndoSwipe(key: ApplicationKey) {
        _uiState.update {
            it.copy(keysToBeRemoved = it.keysToBeRemoved - key)
        }
    }

    /**
     * Remove a given application key from the network.
     *
     * @param key Key to be removed.
     */
    internal fun remove(key: ApplicationKey) {
        _uiState.update {
            it.copy(keysToBeRemoved = it.keysToBeRemoved - key)
        }
        network.remove(key)
        save()
    }

    /**
     * Removes all keys that are queued for deletion.
     */
    private fun removeKeys() {
        _uiState.value.keysToBeRemoved.forEach {
            network.remove(it)
        }
        save()
    }

    /**
     * Saves the network.
     */
    private fun save() {
        viewModelScope.launch {
            repository.save()
        }
    }
}

data class ApplicationKeysScreenUiState internal constructor(
    val keys: List<ApplicationKey> = listOf(),
    val keysToBeRemoved: List<ApplicationKey> = listOf()
)
