@file:Suppress("unused")
package no.nordicsemi.android.nrfmesh.feature.provisioners.navigation

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.provisioners.ranges.GroupRangesRoute
import no.nordicsemi.android.nrfmesh.feature.provisioners.ranges.GroupRangesViewModel
import java.util.UUID

object GroupRangesDestination : MeshNavigationDestination {
    const val arg = "rangesUuidArg"
    override val route: String = "group_ranges_route/{$arg}"
    override val destination: String = "group_ranges_destination"

    /**
     * Creates destination route for a provisioner UUID.
     */
    fun createNavigationRoute(provisionerUuid: UUID): String =
        "group_ranges_route/${Uri.encode(provisionerUuid.toString())}"

    /**
     * Returns the provisioner uuid index from a [NavBackStackEntry] after a topic destination
     * navigation call.
     */
    fun fromNavArgs(entry: NavBackStackEntry): String {
        val encodedId = entry.arguments?.getString(arg)!!
        return Uri.decode(encodedId)
    }
}

fun NavGraphBuilder.groupRangesGraph(
    appState: AppState,
    onBackPressed: () -> Unit
) {
    composable(route = GroupRangesDestination.route) {
        val viewModel = hiltViewModel<GroupRangesViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        GroupRangesRoute(
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