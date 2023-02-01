package no.nordicsemi.android.nrfmesh.feature.scenes.destination

import no.nordicsemi.android.common.navigation.createSimpleDestination
import no.nordicsemi.android.common.navigation.defineDestination

val scenes = createSimpleDestination("scenes")

val scenesDestination = defineDestination(scenes) {}

val scenesDestinations = scenesDestination + sceneDestination