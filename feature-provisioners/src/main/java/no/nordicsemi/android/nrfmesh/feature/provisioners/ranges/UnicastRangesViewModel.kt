package no.nordicsemi.android.nrfmesh.feature.provisioners.ranges

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import no.nordicsemi.android.nrfmesh.feature.provisioners.destinations.unicastRanges
import no.nordicsemi.kotlin.mesh.core.model.Range
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.UnicastRange
import java.util.*
import javax.inject.Inject

@HiltViewModel
internal class UnicastRangesViewModel @Inject internal constructor(
    savedStateHandle: SavedStateHandle,
    navigator: Navigator,
    repository: DataStoreRepository
) : RangesViewModel(savedStateHandle, navigator, repository) {

    override var uuid: UUID = parameterOf(unicastRanges)
    override fun getRanges(): List<Range> = provisioner.allocatedUnicastRanges

    override fun getOtherRanges(): List<Range> = getOtherProvisioners()
            .flatMap { it.allocatedUnicastRanges }

    override fun onAddRangeClicked(): Range = network.nextAvailableUnicastAddressRange(
        rangeSize = 0x199A
    ) ?: UnicastRange(
        lowAddress = UnicastAddress(0x0001u),
        highAddress = UnicastAddress(0x7FFFu)
    )
}
