package no.nordicsemi.android.nrfmesh.feature.provisioners.ranges

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import no.nordicsemi.android.nrfmesh.feature.provisioners.destinations.groupRanges
import no.nordicsemi.kotlin.mesh.core.model.*
import javax.inject.Inject

@HiltViewModel
internal class GroupRangesViewModel @Inject internal constructor(
    savedStateHandle: SavedStateHandle,
    navigator: Navigator,
    repository: DataStoreRepository
) : RangesViewModel(savedStateHandle, navigator, repository) {

    override fun getDestinationId() = groupRanges
    override fun getAllocatedRanges(): List<Range> = provisioner.allocatedGroupRanges

    override fun getOtherRanges(): List<Range> = getOtherProvisioners()
        .flatMap { it.allocatedGroupRanges }
        .toList()

    override fun addRange(start: UInt, end: UInt) = runCatching {
        val range = GroupAddress(start.toUShort())..GroupAddress(end.toUShort())
        _uiState.value = with(_uiState.value) {
            copy(ranges = ranges + range)
        }
    }

}