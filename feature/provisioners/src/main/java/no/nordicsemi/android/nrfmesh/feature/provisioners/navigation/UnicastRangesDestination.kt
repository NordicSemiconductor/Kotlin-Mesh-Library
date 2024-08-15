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
import no.nordicsemi.android.nrfmesh.feature.provisioners.ranges.UnicastRangesRoute
import no.nordicsemi.android.nrfmesh.feature.provisioners.ranges.UnicastRangesViewModel
import java.util.UUID

object UnicastRangesDestination : MeshNavigationDestination {
    const val arg = "rangesUuidArg"
    override val route: String = "unicast_ranges_route/{$arg}"
    override val destination: String = "unicast_ranges_destination"

    /**
     * Creates destination route for a provisioner UUID.
     */
    fun createNavigationRoute(provisionerUuid: UUID): String =
        "unicast_ranges_route/${Uri.encode(provisionerUuid.toString())}"

    /**
     * Returns the provisioner uuid index from a [NavBackStackEntry] after a topic destination
     * navigation call.
     */
    fun fromNavArgs(entry: NavBackStackEntry): String {
        val encodedId = entry.arguments?.getString(arg)!!
        return Uri.decode(encodedId)
    }
}

fun NavGraphBuilder.unicastRangesGraph(
    appState: AppState,
    onBackPressed: () -> Unit
) {
    composable(route = UnicastRangesDestination.route) {
        val viewModel = hiltViewModel<UnicastRangesViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        UnicastRangesRoute(
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