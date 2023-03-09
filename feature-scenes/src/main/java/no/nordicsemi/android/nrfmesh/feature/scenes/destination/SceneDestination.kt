@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.scenes.destination

import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.scenes.SceneRoute
import no.nordicsemi.android.nrfmesh.feature.scenes.SceneViewModel

val scene = createDestination<Int, Unit>("scene")

val sceneDestination = defineDestination(scene) {
    val viewModel: SceneViewModel = hiltViewModel()
    SceneRoute(viewModel = viewModel)
}