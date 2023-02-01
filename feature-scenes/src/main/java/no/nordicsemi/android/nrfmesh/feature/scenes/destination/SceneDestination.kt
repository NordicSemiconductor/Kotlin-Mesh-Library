@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.scenes.destination

import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.kotlin.mesh.core.model.SceneNumber

val scene = createDestination<SceneNumber, Unit>("scene")

val sceneDestination = defineDestination(scene) {}