package no.nordicsemi.android.nrfmesh.feature.application.keys

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.DestinationId
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import javax.inject.Inject

@HiltViewModel
internal class ApplicationKeysViewModel @Inject internal constructor(
    private val navigator: Navigator,
    private val repository: DataStoreRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ApplicationKeysScreenUiState(listOf()))
    val uiState: StateFlow<ApplicationKeysScreenUiState> = _uiState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ApplicationKeysScreenUiState()
    )

    private lateinit var network: MeshNetwork
    private var keysToBeRemoved = mutableListOf<ApplicationKey>()

    init {
        viewModelScope.launch {
            repository.network.collect { network ->
                this@ApplicationKeysViewModel.network = network
                _uiState.value = ApplicationKeysScreenUiState(keys = filterKeysToBeRemoved())
            }
        }
    }

    internal fun navigate(destinationId: DestinationId<Int, Unit>, keyIndex: Int) {
        navigator.navigateTo(destinationId, keyIndex)
    }

    /**
     * Adds an application key to the network.
     */
    internal fun addApplicationKey(): ApplicationKey {
        // Let's delete any keys that are queued for deletion before adding a new.
        removeKeys()
        return network.add(
            name = "nRF Application Key",
            boundNetworkKey = network.networkKeys.first()
        )
    }

    /**
     * Invoked when a key is swiped to be deleted. The given key is added to a list of keys that
     * is to be deleted.
     *
     * @param key Application key to be deleted.
     */
    fun onSwiped(key: ApplicationKey) {
        if (!keysToBeRemoved.contains(key))
            keysToBeRemoved.add(key)
        if (keysToBeRemoved.size == network.applicationKeys.size)
            _uiState.value = ApplicationKeysScreenUiState(keys = filterKeysToBeRemoved())
    }

    /**
     * Invoked when a key is swiped to be deleted is undone. When invoked the given key is removed
     * from the list of keys to be deleted.
     *
     * @param key Application key to be reverted.
     */
    fun onUndoSwipe(key: ApplicationKey) {
        keysToBeRemoved.remove(key)
        if (keysToBeRemoved.isEmpty())
            _uiState.value = ApplicationKeysScreenUiState(keys = filterKeysToBeRemoved())
    }

    /**
     * Remove a given application key from the network.
     *
     * @param key Key to be removed.
     */
    internal fun remove(key: ApplicationKey) {
        network.apply {
            applicationKeys.find { it == key }?.let {
                remove(it)
            }
        }
        keysToBeRemoved.remove(key)
    }

    /**
     * Removes the keys from a network.
     */
    internal fun removeKeys() {
        remove()
        save()
    }

    /**
     * Removes the keys from the network.
     */
    private fun remove() {
        network.applicationKeys.filter {
            it in keysToBeRemoved
        }.forEach {
            network.remove(it)
        }
        keysToBeRemoved.clear()
    }

    /**
     * Saves the network.
     */
    private fun save() {
        viewModelScope.launch {
            repository.save()
        }
    }

    private fun filterKeysToBeRemoved() = network.applicationKeys.filter {
        it !in keysToBeRemoved
    }
}

data class ApplicationKeysScreenUiState internal constructor(
    val keys: List<ApplicationKey> = listOf()
)
