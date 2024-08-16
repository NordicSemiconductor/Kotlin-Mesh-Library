package no.nordicsemi.android.nrfmesh.feature.ranges

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.kotlin.mesh.core.model.GroupAddress
import no.nordicsemi.kotlin.mesh.core.model.Range
import no.nordicsemi.kotlin.mesh.core.model.plus
import javax.inject.Inject

@HiltViewModel
internal class GroupRangesViewModel @Inject internal constructor(
    savedStateHandle: SavedStateHandle,
    repository: CoreDataRepository
) : RangesViewModel(savedStateHandle = savedStateHandle, repository = repository) {

    override fun getAllocatedRanges(): List<Range> = provisioner.allocatedGroupRanges

    override fun getOtherRanges(): List<Range> = getOtherProvisioners()
        .flatMap { it.allocatedGroupRanges }
        .toList()

    override fun addRange(start: UInt, end: UInt) {
        val range = GroupAddress(start.toUShort())..GroupAddress(end.toUShort())
        _uiState.update {
            it.copy(ranges = it.ranges + range)
        }
        if (!_uiState.value.conflicts) {
            allocate()
            save()
        }
    }

    override fun onRangeUpdated(range: Range, low: UShort, high: UShort) {
        updateRange(range, GroupAddress(address = low)..GroupAddress(address = high))
    }

    override fun isValidBound(bound: UShort): Boolean = when {
        GroupAddress.isValid(address = bound) -> true
        else -> throw Throwable("Invalid group address")
    }
}