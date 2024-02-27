package no.nordicsemi.android.nrfmesh.feature.provisioners.ranges

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.feature.provisioners.destinations.groupRanges
import no.nordicsemi.kotlin.mesh.core.model.*
import javax.inject.Inject

@HiltViewModel
internal class GroupRangesViewModel @Inject internal constructor(
    savedStateHandle: SavedStateHandle,
    navigator: Navigator,
    repository: CoreDataRepository
) : RangesViewModel(savedStateHandle, navigator, repository) {

    override fun getDestinationId() = groupRanges
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