package no.nordicsemi.android.nrfmesh.feature.scenes.navigation

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination.Companion.ARG
import no.nordicsemi.android.nrfmesh.feature.scenes.SceneRoute
import no.nordicsemi.android.nrfmesh.feature.scenes.SceneViewModel
import no.nordicsemi.kotlin.mesh.core.model.SceneNumber

object SceneDestination : MeshNavigationDestination {
    override val route: String = "scene_route/{$ARG}"
    override val destination: String = "scene_destination"

    /**
     * Creates destination route for a scene number.
     */
    fun createNavigationRoute(sceneNumberArg: SceneNumber): String =
        "scene_route/${Uri.encode(sceneNumberArg.toInt().toString())}"

    /**
     * Returns the scene number from a [NavBackStackEntry] after a topic destination navigation
     * call.
     */
    fun fromNavArgs(entry: NavBackStackEntry): String {
        val encodedId = entry.arguments?.getString(ARG)!!
        return Uri.decode(encodedId)
    }
}

internal fun NavGraphBuilder.sceneGraph(appState: AppState, onBackPressed: () -> Unit) {
    composable(route = SceneDestination.route) {
        val viewmodel = hiltViewModel<SceneViewModel>()
        val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
        SceneRoute(
            appState = appState,
            uiState = uiState,
            onNameChanged = viewmodel::onNameChanged,
            onBackPressed = onBackPressed
        )
    }
}