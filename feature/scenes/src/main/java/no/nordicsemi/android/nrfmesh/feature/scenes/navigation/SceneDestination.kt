package no.nordicsemi.android.nrfmesh.feature.scenes.navigation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination.Companion.ARG
import no.nordicsemi.android.nrfmesh.feature.scenes.SceneRoute
import no.nordicsemi.kotlin.mesh.core.model.Scene
import no.nordicsemi.kotlin.mesh.core.model.SceneNumber

@Parcelize
data class SceneRoute(val number: SceneNumber) : Parcelable

object SceneDestination : MeshNavigationDestination {
    override val route: String = "scene_route/{$ARG}"
    override val destination: String = "scene_destination"
}

@Composable
fun SceneScreenRoute(scene: Scene, save: () -> Unit) {
    SceneRoute(scene = scene, save = save)
}