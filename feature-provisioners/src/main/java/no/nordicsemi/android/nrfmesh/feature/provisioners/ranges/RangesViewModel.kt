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
    protected lateinit var network: MeshNetwork
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

    internal abstract fun onAddRangeClicked(): Range

    protected abstract fun getRanges(): List<Range>

    /**
     * Adds a range to the network.
     */
    internal fun addRange(range: Range): Provisioner {
        provisioner.allocate(range = range)
        return provisioner
    }

    /**
     * Invoked when a range is swiped to be deleted. The given range is added to a list
     * of ranges to be deleted.
     *
     * @param range Provisioner to be deleted.
     */
    internal fun onSwiped(range: Range) {
        if (!rangesToBeRemoved.contains(range))
            rangesToBeRemoved.add(range)
        // TODO Fix this
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
        // TODO Fix this
        if (rangesToBeRemoved.isEmpty()) {
            /*_uiState.value =
                UnicastRangesScreenUiState(ranges = filterProvisionersTobeRemoved())*/
        }
    }

    /**
     * Remove a given range from the provisioner.
     *
     * @param range range to be removed.
     */
    internal fun remove(range: Range) {
        // TODO Fix this
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
        // TODO Fix this
    }

    /**
     * Saves the network.
     */
    private fun save() {
        viewModelScope.launch { repository.save() }
    }
}

data class RangesScreenUiState internal constructor(val ranges: List<Range> = listOf())