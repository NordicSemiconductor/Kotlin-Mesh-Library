@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.provisioners.navigation

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.provisioners.ranges.RangesRoute
import no.nordicsemi.android.nrfmesh.feature.provisioners.ranges.RangesViewModel
import java.util.UUID


object RangesDestination : MeshNavigationDestination {
    const val rangesUuidArg = "rangesUuidArg"
    override val route: String = "provisioner_route/{$rangesUuidArg}"
    override val destination: String = "ranges_destination"

    /**
     * Creates destination route for a provisioner UUID.
     */
    fun createNavigationRoute(provisionerUuid: UUID): String =
        "ranges_route/${Uri.encode(provisionerUuid.toString())}"

    /**
     * Returns the provisioner uuid index from a [NavBackStackEntry] after a topic destination
     * navigation call.
     */
    fun fromNavArgs(entry: NavBackStackEntry): String {
        val encodedId = entry.arguments?.getString(rangesUuidArg)!!
        return Uri.decode(encodedId)
    }
}

fun NavGraphBuilder.unicastRangesGraph(onBackPressed: () -> Unit) {
    composable(route = RangesDestination.route) {
        val viewModel = hiltViewModel<RangesViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        RangesRoute(
            uiState = uiState,
            addRange = viewModel::addRange,
            onRangeUpdated = viewModel::onRangeUpdated,
            onSwiped = viewModel::onSwiped,
            onUndoClicked = viewModel::onUndoSwipe,
            remove = viewModel::remove,
            resolve = viewModel::resolve,
            isValidBound = viewModel::isValidBound
        )
    }
}

fun NavGraphBuilder.groupRangesGraph(onBackPressed: () -> Unit) {
    composable(route = RangesDestination.route) {
        val viewModel = hiltViewModel<RangesViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        RangesRoute(
            uiState = uiState,
            addRange = viewModel::addRange,
            onRangeUpdated = viewModel::onRangeUpdated,
            onSwiped = viewModel::onSwiped,
            onUndoClicked = viewModel::onUndoSwipe,
            remove = viewModel::remove,
            resolve = viewModel::resolve,
            isValidBound = viewModel::isValidBound
        )
    }
}

fun NavGraphBuilder.sceneRangesGraph(onBackPressed: () -> Unit) {
    composable(route = RangesDestination.route) {
        val viewModel = hiltViewModel<RangesViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        RangesRoute(
            uiState = uiState,
            addRange = viewModel::addRange,
            onRangeUpdated = viewModel::onRangeUpdated,
            onSwiped = viewModel::onSwiped,
            onUndoClicked = viewModel::onUndoSwipe,
            remove = viewModel::remove,
            resolve = viewModel::resolve,
            isValidBound = viewModel::isValidBound
        )
    }
}