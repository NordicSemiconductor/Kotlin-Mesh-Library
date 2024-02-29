@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.provisioners.destinations

import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.provisioners.ranges.GroupRangesViewModel
import no.nordicsemi.android.nrfmesh.feature.provisioners.ranges.RangesRoute
import no.nordicsemi.android.nrfmesh.feature.provisioners.ranges.SceneRangesViewModel
import no.nordicsemi.android.nrfmesh.feature.provisioners.ranges.UnicastRangesViewModel
import java.util.UUID

val unicastRanges = createDestination<UUID, Unit>("unicast_ranges")
val unicastRangesDestination = defineDestination(unicastRanges) {
    val viewModel: UnicastRangesViewModel = hiltViewModel()
    RangesRoute(viewModel = viewModel)
}

val groupRanges = createDestination<UUID, Unit>("group_ranges")
val groupRangesDestination = defineDestination(groupRanges) {
    val viewModel: GroupRangesViewModel = hiltViewModel()
    RangesRoute(viewModel = viewModel)
}

val sceneRanges = createDestination<UUID, Unit>("scene_ranges")
val sceneRangesDestination = defineDestination(sceneRanges) {
    val viewModel: SceneRangesViewModel = hiltViewModel()
    RangesRoute(viewModel = viewModel)
}