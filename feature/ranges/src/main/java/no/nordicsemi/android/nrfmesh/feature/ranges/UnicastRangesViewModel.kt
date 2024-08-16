package no.nordicsemi.android.nrfmesh.feature.ranges

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.kotlin.mesh.core.model.Range
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.plus
import javax.inject.Inject

@HiltViewModel
internal class UnicastRangesViewModel @Inject internal constructor(
    savedStateHandle: SavedStateHandle,
    repository: CoreDataRepository
) : RangesViewModel(savedStateHandle = savedStateHandle, repository = repository) {

    override fun getAllocatedRanges(): List<Range> = provisioner.allocatedUnicastRanges

    override fun getOtherRanges(): List<Range> = getOtherProvisioners()
        .flatMap { it.allocatedUnicastRanges }
        .toList()

    override fun addRange(start: UInt, end: UInt) {
        viewModelScope.launch {
            val range = (UnicastAddress(start.toUShort())..UnicastAddress(end.toUShort()))
            _uiState.update {
                it.copy(ranges = it.ranges + range)
            }
            if (!_uiState.value.conflicts) {
                allocate()
                save()
            }
        }
    }

    override fun onRangeUpdated(range: Range, low: UShort, high: UShort) {
        updateRange(range, UnicastAddress(address = low)..UnicastAddress(address = high))
    }

    override fun isValidBound(bound: UShort): Boolean = when {
        UnicastAddress.isValid(address = bound) -> true
        else -> throw Throwable("Invalid unicast address")
    }
}