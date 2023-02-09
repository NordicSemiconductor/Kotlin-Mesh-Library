package no.nordicsemi.android.nrfmesh.feature.provisioners.ranges

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import no.nordicsemi.android.nrfmesh.feature.provisioners.destinations.groupRanges
import no.nordicsemi.kotlin.mesh.core.model.*
import java.util.*
import javax.inject.Inject

@HiltViewModel
internal class GroupRangesViewModel @Inject internal constructor(
    savedStateHandle: SavedStateHandle,
    navigator: Navigator,
    repository: DataStoreRepository
) : RangesViewModel(savedStateHandle, navigator, repository) {

    override var uuid: UUID = parameterOf(groupRanges)
    override fun getRanges(): List<Range> = provisioner.allocatedGroupRanges

    override fun onAddRangeClicked(): Range = network.nextAvailableGroupAddressRange(
        rangeSize = 0x199A
    ) ?: GroupRange(
        lowAddress = GroupAddress(0xC000u),
        highAddress = GroupAddress(0xFEFFu)
    )
}