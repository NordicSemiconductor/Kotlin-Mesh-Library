package no.nordicsemi.android.nrfmesh.feature.scenes.navigation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.feature.scenes.SceneRoute
import no.nordicsemi.kotlin.mesh.core.model.Scene
import no.nordicsemi.kotlin.mesh.core.model.SceneNumber

@Serializable
@Parcelize
data class SceneContent(val number: SceneNumber) : Parcelable

@Composable
fun SceneScreenRoute(scene: Scene, save: () -> Unit) {
    SceneRoute(scene = scene, save = save)
}