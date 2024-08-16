package no.nordicsemi.android.nrfmesh.feature.ranges

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.kotlin.mesh.core.model.Range
import no.nordicsemi.kotlin.mesh.core.model.Scene
import no.nordicsemi.kotlin.mesh.core.model.SceneRange
import no.nordicsemi.kotlin.mesh.core.model.plus
import javax.inject.Inject

@HiltViewModel
internal class SceneRangesViewModel @Inject internal constructor(
    savedStateHandle: SavedStateHandle,
    repository: CoreDataRepository
) : RangesViewModel(savedStateHandle = savedStateHandle, repository = repository) {

    override fun getAllocatedRanges(): List<Range> = provisioner.allocatedSceneRanges

    override fun getOtherRanges(): List<Range> = getOtherProvisioners()
        .flatMap { it.allocatedSceneRanges }
        .toList()

    override fun addRange(start: UInt, end: UInt) {
        val range = SceneRange(start.toUShort(), end.toUShort())
        _uiState.update {
            it.copy(ranges = it.ranges + range)
        }
        if (!_uiState.value.conflicts) {
            allocate()
            save()
        }
    }

    override fun onRangeUpdated(range: Range, low: UShort, high: UShort) {
        updateRange(range, SceneRange(low, high))
    }

    override fun isValidBound(bound: UShort): Boolean = when {
        Scene.isValid(sceneNumber = bound) -> true
        else -> throw Throwable("Invalid unicast address")
    }
}