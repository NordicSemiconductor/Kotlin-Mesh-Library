package no.nordicsemi.android.nrfmesh.feature.provisioners.ranges

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import no.nordicsemi.android.nrfmesh.feature.provisioners.destinations.sceneRanges
import no.nordicsemi.kotlin.mesh.core.model.Range
import java.util.*
import javax.inject.Inject

@HiltViewModel
internal class SceneRangesViewModel @Inject internal constructor(
    savedStateHandle: SavedStateHandle,
    navigator: Navigator,
    repository: DataStoreRepository
) : RangesViewModel(savedStateHandle, navigator, repository) {

    override var uuid: UUID = parameterOf(sceneRanges)
    override fun getRanges(): List<Range> = provisioner.allocatedSceneRanges
}