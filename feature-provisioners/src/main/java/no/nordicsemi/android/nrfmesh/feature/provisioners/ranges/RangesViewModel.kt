package no.nordicsemi.android.nrfmesh.feature.provisioners.ranges

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.DestinationId
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.kotlin.mesh.core.model.*
import java.util.*

@Suppress("ConvertArgumentToSet")
internal abstract class RangesViewModel(
    savedStateHandle: SavedStateHandle,
    navigator: Navigator,
    private val repository: CoreDataRepository
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
        // Remove the ranges that were swiped for removal.
        remove()
        // Resolve any conflicts if they are not resolved already.
        resolve()
        // Allocate the newly added ranges to the provisioner.
        allocate()
        save()
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
     * Returns the list of ranges allocated to other provisioners in the network.
     * @return list of ranges of other provisioners.
     */
    protected abstract fun getOtherRanges(): List<Range>

    /**
     * Adds a range to the network.
     *
     * @param start start address of the range.
     * @param end end address of the range.
     */
    internal abstract fun addRange(start: UInt, end: UInt)

    /**
     * Checks if the given bound is valid.
     *
     * @param bound bound to be checked.
     * @return true if valid, false otherwise.
     */
    internal abstract fun isValidBound(bound: UShort): Boolean

    /**
     * Invoked when the user updates a given range
     *
     * @param range range to be updated.
     * @param low low address of the new range.
     * @param high high address of the new range.
     */
    internal abstract fun onRangeUpdated(range: Range, low: UShort, high: UShort)

    /**
     * Updates the given range with the given new range.
     *
     * @param range range to be updated.
     * @param newRange new range to be updated with.
     */
    protected fun updateRange(range: Range, newRange: Range) {
        _uiState.value = with(_uiState.value) {
            copy(ranges = ranges.map {
                if (it == range) newRange else it
            })
        }
        if (!_uiState.value.conflicts) {
            provisioner.update(range, newRange)
        }
    }

    /**
     * Returns the list of other provisioners in the network.
     */
    protected fun getOtherProvisioners(): List<Provisioner> = network.provisioners
        .filter { it.uuid != uuid }

    /**
     * Resolves any conflicting ranges with the other ranges.
     */
    internal fun resolve() {
        _uiState.value = with(_uiState.value) {
            copy(ranges = ranges - otherRanges)
        }
    }

    protected fun allocate() {
        _uiState.value.ranges
            .filter { it !in getAllocatedRanges() }
            .forEach {
                runCatching {
                    provisioner.allocate(it)
                }
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
        if (rangesToBeRemoved.size == getAllocatedRanges().size)
            _uiState.value = _uiState.value.copy(ranges = filterRangesToBeRemoved())
    }

    /**
     * Invoked when a ranges that is swiped to be deleted is undone. When invoked the given
     * range is removed from the list of ranges to be deleted.
     *
     * @param range Scene to be reverted.
     */
    internal fun onUndoSwipe(range: Range) {
        rangesToBeRemoved.remove(range)
        if (rangesToBeRemoved.isEmpty())
            _uiState.value = _uiState.value.copy(ranges = filterRangesToBeRemoved())
    }

    /**
     * Filters the ranges to be removed from the list of ranges.
     */
    private fun filterRangesToBeRemoved() = _uiState.value.ranges.filter {
        it !in rangesToBeRemoved
    }

    /**
     * Remove a given range from the provisioner.
     *
     * @param range range to be removed.
     */
    internal fun remove(range: Range) {
        rangesToBeRemoved.find { it == range }?.let {
            rangesToBeRemoved.add(it)
        }
    }

    /**
     * Removes the selected ranges from the network.
     */
    private fun remove() {
        getAllocatedRanges().filter {
            it in rangesToBeRemoved
        }.forEach {
            provisioner.remove(it)
        }
        rangesToBeRemoved.clear()
    }

    /**
     * Saves the network.
     */
    private fun save() {
        viewModelScope.launch { repository.save() }
    }

    /*internal fun isValidBound(range: Range, bound: UShort): Boolean = when (range) {
        is UnicastRange -> if (UnicastAddress.isValid(address = bound)) true
        else throw Throwable("Invalid unicast address")
        is GroupRange -> if (GroupAddress.isValid(address = bound)) true
        else throw Throwable("Invalid group address")
        is SceneRange -> if (!Scene.isValid(sceneNumber = bound)) true
        else throw Throwable("Invalid scene number")
    }*/
}

data class RangesScreenUiState internal constructor(
    val ranges: List<Range> = listOf(),
    val otherRanges: List<Range> = listOf()
) {
    val conflicts: Boolean
        get() = ranges.overlaps(otherRanges)
}
