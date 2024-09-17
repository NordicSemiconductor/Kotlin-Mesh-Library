package no.nordicsemi.android.nrfmesh.feature.elements.navigation

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination.Companion.ARG
import no.nordicsemi.android.nrfmesh.feature.elements.ElementRoute
import no.nordicsemi.android.nrfmesh.feature.elements.ElementViewModel
import java.util.UUID

object ElementDestination : MeshNavigationDestination {
    internal const val ELEMENT_INDEX = "ELEMENT_INDEX"
    override val route: String = "elements_route/{$ARG}/{$ELEMENT_INDEX}"
    override val destination: String = "elements_destination"

    /**
     * Creates destination route for a network key index.
     *
     * @param uuid UUID of the element.
     */
    fun createNavigationRoute(uuid: UUID, index: Int = 0): String =
        "elements_route/${Uri.encode(uuid.toString())}/${Uri.encode(index.toString())}"
}

fun NavGraphBuilder.elementsGraph(
    appState: AppState,
    onNavigateToDestination: (MeshNavigationDestination, String) -> Unit,
    onBackPressed: () -> Unit
) {
    composable(route = ElementDestination.route) {
        val viewModel = hiltViewModel<ElementViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ElementRoute(
            appState = appState,
            uiState = uiState,
            onNameChanged = viewModel::onNameChanged,
            navigateToModel = { node ->
                /*onNavigateToDestination(
                    NodeDestination,
                    NodeDestination.createNavigationRoute(node.uuid)
                )*/
            },
            onBackPressed = onBackPressed
        )
    }
    /*nodeGraph(
        appState = appState,
        onNavigateToDestination = onNavigateToDestination,
        onBackPressed = onBackPressed
    )*/
}