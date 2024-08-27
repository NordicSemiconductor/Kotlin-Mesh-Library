@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.ranges.navigation

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
import no.nordicsemi.android.nrfmesh.feature.ranges.SceneRangesRoute
import no.nordicsemi.android.nrfmesh.feature.ranges.SceneRangesViewModel
import java.util.UUID

data object SceneRangesDestination : MeshNavigationDestination {
    override val route: String = "scene_ranges_route/{$ARG}"
    override val destination: String = "scene_ranges_destination"

    /**
     * Creates destination route for a provisioner UUID.
     */
    fun createNavigationRoute(provisionerUuid: UUID): String =
        "scene_ranges_route/${Uri.encode(provisionerUuid.toString())}"

    /**
     * Returns the provisioner uuid index from a [NavBackStackEntry] after a topic destination
     * navigation call.
     */
    fun fromNavArgs(entry: NavBackStackEntry): String {
        val encodedId = entry.arguments?.getString(ARG)!!
        return Uri.decode(encodedId)
    }
}

fun NavGraphBuilder.sceneRangesGraph(
    appState: AppState,
    onBackPressed: () -> Unit
) {
    composable(route = SceneRangesDestination.route) {
        val viewModel = hiltViewModel<SceneRangesViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        SceneRangesRoute(
            appState = appState,
            uiState = uiState,
            addRange = viewModel::addRange,
            onRangeUpdated = viewModel::onRangeUpdated,
            onSwiped = viewModel::onSwiped,
            onUndoClicked = viewModel::onUndoSwipe,
            remove = viewModel::remove,
            resolve = viewModel::resolve,
            isValidBound = viewModel::isValidBound,
            onBackPressed = onBackPressed
        )
    }
}