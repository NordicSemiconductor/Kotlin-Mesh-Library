package no.nordicsemi.android.nrfmesh.feature.scenes.destination

import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.common.navigation.createSimpleDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.scenes.ScenesRoute
import no.nordicsemi.android.nrfmesh.feature.scenes.ScenesViewModel

val scenes = createSimpleDestination("scenes")

val scenesDestination = defineDestination(scenes) {
    val viewModel: ScenesViewModel = hiltViewModel()
    ScenesRoute(viewModel = viewModel, navigateToScene = { sceneNumber ->
        viewModel.navigateTo(scene, sceneNumber.toInt())
    })
}

val scenesDestinations = scenesDestination + sceneDestination