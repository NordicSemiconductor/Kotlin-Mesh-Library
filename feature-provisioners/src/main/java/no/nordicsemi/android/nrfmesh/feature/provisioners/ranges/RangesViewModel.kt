package no.nordicsemi.android.nrfmesh.feature.provisioners.ranges

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import no.nordicsemi.kotlin.mesh.core.model.Range
import java.util.*

internal abstract class RangesViewModel(
    savedStateHandle: SavedStateHandle,
    navigator: Navigator,
    private val repository: DataStoreRepository
) : SimpleNavigationViewModel(navigator, savedStateHandle) {

    private var rangesToBeRemoved = mutableListOf<Range>()
    private lateinit var network: MeshNetwork
    protected lateinit var provisioner: Provisioner
    protected open lateinit var uuid: UUID

    private val _uiState = MutableStateFlow(RangesScreenUiState(listOf()))
    val uiState: StateFlow<RangesScreenUiState> = repository.network.map { network ->
        this@RangesViewModel.network = network
        RangesScreenUiState(ranges = network.provisioner((uuid))?.let { provisioner ->
            this@RangesViewModel.provisioner = provisioner
            getRanges()
        } ?: listOf())
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        RangesScreenUiState()
    )

    init {
        viewModelScope.launch {
            repository.network.collect { network ->
                this@RangesViewModel.network = network
                _uiState.value = RangesScreenUiState(ranges = listOf())
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        removeRanges()
    }

    protected abstract fun getRanges(): List<Range>

    /**
     * Adds a scene to the network.
     */
    internal fun addRange(): Provisioner {
        removeRanges()
        val provisioner = Provisioner()
        network.run {
            nextAvailableUnicastAddressRange(rangeSize = 0x199A)?.let { range ->
                provisioner.allocate(range)
            }
            nextAvailableGroupAddressRange(rangeSize = 0x0C9A)?.let { range ->
                provisioner.allocate(range)
            }
            nextAvailableSceneRange(rangeSize = 0x3334)?.let { range ->
                provisioner.allocate(range)
            }
            add(provisioner = provisioner, address = null)
        }
        return provisioner
    }

    /**
     * Invoked when a provisioner is swiped to be deleted. The given provisioner is added to a list
     * of provisioners to be deleted.
     *
     * @param provisioner Provisioner to be deleted.
     */
    internal fun onSwiped(provisioner: Range) {
        if (!rangesToBeRemoved.contains(provisioner))
            rangesToBeRemoved.add(provisioner)
        if (rangesToBeRemoved.size == network.scenes.size) {
            /*_uiState.value =
                UnicastRangesScreenUiState(ranges = filterProvisionersTobeRemoved())*/
        }
    }

    /**
     * Invoked when a provisioner is swiped to be deleted is undone. When invoked the given
     * provisioner is removed from the list of provisioners to be deleted.
     *
     * @param provisioner Scene to be reverted.
     */
    internal fun onUndoSwipe(provisioner: Range) {
        rangesToBeRemoved.remove(provisioner)
        if (rangesToBeRemoved.isEmpty()) {
            /*_uiState.value =
                UnicastRangesScreenUiState(ranges = filterProvisionersTobeRemoved())*/
        }
    }

    /**
     * Remove a given scene from the network.
     *
     * @param provisioner Scene to be removed.
     */
    internal fun remove(provisioner: Range) {

    }

    /**
     * Removes the range from a network.
     */
    private fun removeRanges() {
        remove()
        save()
    }

    /**
     * Removes the selected provisioners from the network.
     */
    private fun remove() {

    }

    /**
     * Saves the network.
     */
    private fun save() {
        viewModelScope.launch { repository.save() }
    }
}

data class RangesScreenUiState internal constructor(val ranges: List<Range> = listOf())