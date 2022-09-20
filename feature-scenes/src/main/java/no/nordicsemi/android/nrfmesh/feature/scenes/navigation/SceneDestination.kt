@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.scenes.navigation

import android.net.Uri
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.feature.scenes.SceneRoute
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.kotlin.mesh.core.model.SceneNumber

object SceneDestination : MeshNavigationDestination {
    const val sceneNumberArg = "sceneNumberArg"
    override val route: String = "scene_route/{$sceneNumberArg}"
    override val destination: String = "scene_destination"

    /**
     * Creates destination route for a scene number.
     */
    fun createNavigationRoute(sceneNumberArg: SceneNumber): String =
        "scene_route/${Uri.encode(sceneNumberArg.toInt().toString())}"

    /**
     * Returns the application key index from a [NavBackStackEntry] after a topic destination
     * navigation call.
     */
    fun fromNavArgs(entry: NavBackStackEntry): String {
        val encodedId = entry.arguments?.getString(sceneNumberArg)!!
        return Uri.decode(encodedId)
    }
}

internal fun NavGraphBuilder.sceneGraph(onBackPressed: () -> Unit) {
    composable(route = SceneDestination.route) {
        SceneRoute(onBackPressed = onBackPressed)
    }
}