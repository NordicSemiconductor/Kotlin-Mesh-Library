package no.nordicsemi.android.nrfmesh.feature.ranges

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import no.nordicsemi.kotlin.mesh.core.model.Range
import no.nordicsemi.kotlin.mesh.core.model.minus
import no.nordicsemi.kotlin.mesh.core.model.overlaps
import no.nordicsemi.kotlin.mesh.core.model.plus
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Suppress("ConvertArgumentToSet")
internal abstract class RangesViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: CoreDataRepository
) : ViewModel() {

    protected lateinit var network: MeshNetwork
    protected lateinit var provisioner: Provisioner

    private val uuid: Uuid = checkNotNull(savedStateHandle[MeshNavigationDestination.ARG]).let {
        Uuid.parse(uuidString = it as String)
    }

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
                val ranges = network.provisioner(uuid)?.let { provisioner ->
                    this@RangesViewModel.provisioner = provisioner
                    getAllocatedRanges()
                } ?: emptyList()

                _uiState.update { state ->
                    state.copy(
                        ranges = ranges,
                        otherRanges = getOtherRanges(),
                        rangesToBeRemoved = ranges.filter { it in state.rangesToBeRemoved }
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Remove the ranges that were swiped for removal.
        removeRanges()
    }

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
     * @param start Start address of the range.
     * @param end   End address of the range.
     */
    internal abstract fun addRange(start: UInt, end: UInt)

    /**
     * Checks if the given bound is valid.
     *
     * @param bound Bound to be checked.
     * @return true if valid, false otherwise.
     */
    internal abstract fun isValidBound(bound: UShort): Boolean

    /**
     * Invoked when the user updates a given range
     *
     * @param range Range to be updated.
     * @param low   Low address of the new range.
     * @param high  High address of the new range.
     */
    internal abstract fun onRangeUpdated(range: Range, low: UShort, high: UShort)

    /**
     * Updates the given range with the given new range.
     *
     * @param range    Range to be updated.
     * @param newRange New range to be updated with.
     */
    protected fun updateRange(range: Range, newRange: Range) {
        _uiState.update { state ->
            state.copy(ranges = state.ranges.map {
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
        _uiState.update {
            it.copy(ranges = it.ranges - it.otherRanges)
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
     * @param range Range to be deleted.
     */
    internal fun onSwiped(range: Range) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(rangesToBeRemoved = it.rangesToBeRemoved + range)
            }
        }
    }

    /**
     * Invoked when a ranges that is swiped to be deleted is undone. When invoked the given
     * range is removed from the list of ranges to be deleted.
     *
     * @param range Scene to be reverted.
     */
    internal fun onUndoSwipe(range: Range) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(rangesToBeRemoved = it.rangesToBeRemoved - range)
            }
        }
    }

    /**
     * Remove a given range from the provisioner.
     *
     * @param range Range to be removed.
     */
    internal fun remove(range: Range) {
        _uiState.update {
            it.copy(rangesToBeRemoved = it.rangesToBeRemoved - range)
        }
        provisioner.remove(range)
        save()
    }

    /**
     * Removes the ranges that are queued for deletion.
     */
    private fun removeRanges() {
        _uiState.value.rangesToBeRemoved.forEach {
            provisioner.remove(it)
        }
        // Resolve any conflicts if they are not resolved already.
        resolve()
        // Allocate the newly added ranges to the provisioner.
        allocate()
        save()
    }

    /**
     * Saves the network.
     */
    protected fun save() {
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

@ConsistentCopyVisibility
data class RangesScreenUiState internal constructor(
    val ranges: List<Range> = listOf(),
    val otherRanges: List<Range> = listOf(),
    val rangesToBeRemoved: List<Range> = listOf()
) {
    val conflicts: Boolean
        get() = ranges.overlaps(otherRanges)
}
