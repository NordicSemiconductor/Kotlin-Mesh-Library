package no.nordicsemi.android.nrfmesh.feature.provisioners.ranges

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.DestinationId
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import no.nordicsemi.kotlin.mesh.core.model.*
import java.util.*

@Suppress("ConvertArgumentToSet")
internal abstract class RangesViewModel(
    savedStateHandle: SavedStateHandle,
    navigator: Navigator,
    private val repository: DataStoreRepository
) : SimpleNavigationViewModel(navigator, savedStateHandle) {

    protected lateinit var network: MeshNetwork
    protected lateinit var provisioner: Provisioner

    private val uuid: UUID by lazy { parameterOf(getDestinationId()) }

    private var rangesToBeRemoved = mutableListOf<Range>()

    protected val _uiState = MutableStateFlow(RangesScreenUiState(listOf()))
    val uiState: StateFlow<RangesScreenUiState> = _uiState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RangesScreenUiState()
    )

    init {
        viewModelScope.launch {
            repository.network.collect { network ->
                Log.d("RangesViewModel", "Collecting network: $network")
                this@RangesViewModel.network = network
                val ranges1 = network.provisioner(uuid)?.let { provisioner ->
                    this@RangesViewModel.provisioner = provisioner
                    getAllocatedRanges()
                } ?: emptyList()

                _uiState.value = RangesScreenUiState(
                    ranges = ranges1,
                    otherRanges = getOtherRanges()
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        removeRanges()
    }

    /**
     * Returns the destination id that is used to retrieve the user arguments from the saved state
     * handle.
     * @return destination id.
     */
    protected abstract fun getDestinationId(): DestinationId<UUID, Unit>

    /**
     * Returns the list of ranges allocated to the provisioner.
     * @return list of ranges.
     */
    protected abstract fun getAllocatedRanges(): List<Range>

    /**
     * Returns the list of other provisioners in the network.
     */
    protected fun getOtherProvisioners(): List<Provisioner> = network.provisioners
        .filter { it.uuid != uuid }

    /**
     * Returns the list of ranges allocated to other provisioners in the network.
     * @return list of ranges of other provisioners.
     */
    protected abstract fun getOtherRanges(): List<Range>

    /**
     * Adds a range to the network.
     */
    internal abstract fun addRange(start: UInt, end: UInt): Result<Unit>

    internal fun onRangeUpdated(range: Range, newRange: Range) {
        _uiState.value = with(_uiState.value) {
            copy(ranges = ranges.map { if (it == range) newRange else it })
        }
    }

    /**
     * Resolves any conflicting ranges with the other ranges.
     */
    internal fun resolve() {
        _uiState.value = with(_uiState.value) {
            copy(ranges = ranges - otherRanges)
        }
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

data class RangesScreenUiState internal constructor(
    val ranges: List<Range> = listOf(),
    val otherRanges: List<Range> = listOf()
) {
    val conflicts: Boolean
        get() = ranges.overlaps(otherRanges)
}
