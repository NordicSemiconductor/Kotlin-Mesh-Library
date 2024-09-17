package no.nordicsemi.android.nrfmesh.feature.nodes.navigation

import android.net.Uri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.feature.config.networkkeys.navigation.ConfigNetKeysDestination
import no.nordicsemi.android.feature.config.networkkeys.navigation.configNetworkKeysGraph
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination.Companion.ARG
import no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.navigation.ConfigAppKeysDestination
import no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.navigation.configApplicationKeysGraph
import no.nordicsemi.android.nrfmesh.feature.elements.navigation.ElementDestination
import no.nordicsemi.android.nrfmesh.feature.elements.navigation.elementsGraph
import no.nordicsemi.android.nrfmesh.feature.nodes.NodeRoute
import no.nordicsemi.android.nrfmesh.feature.nodes.NodeViewModel
import java.util.UUID

object NodeDestination : MeshNavigationDestination {
    override val route: String = "node_route/{$ARG}"
    override val destination: String = "node_destination"

    /**
     * Creates destination route for a network key index.
     */
    fun createNavigationRoute(uuid: UUID): String =
        "node_route/${Uri.encode(uuid.toString())}"
}

fun NavGraphBuilder.nodeGraph(
    appState: AppState,
    onNavigateToDestination: (MeshNavigationDestination, String) -> Unit,
    onBackPressed: () -> Unit
) {
    composable(route = NodeDestination.route) {
        val viewModel = hiltViewModel<NodeViewModel>()
        val uiState = viewModel.uiState.collectAsStateWithLifecycle()
        NodeRoute(
            appState = appState,
            uiState = uiState.value,
            onRefresh = viewModel::onRefresh,
            onNameChanged = viewModel::onNameChanged,
            onNetworkKeysClicked = {
                onNavigateToDestination(
                    ConfigNetKeysDestination,
                    ConfigNetKeysDestination.createNavigationRoute(it)
                )
            },
            onApplicationKeysClicked = {
                onNavigateToDestination(
                    ConfigAppKeysDestination,
                    ConfigAppKeysDestination.createNavigationRoute(it)
                )
            },
            onElementsClicked = {
                onNavigateToDestination(
                    ElementDestination,
                    ElementDestination.createNavigationRoute(it)
                )
            },
            onGetTtlClicked = { /*TODO*/ },
            onProxyStateToggled = viewModel::onProxyStateToggled,
            onGetProxyStateClicked = viewModel::onGetProxyStateClicked,
            onExcluded = viewModel::onExcluded,
            onResetClicked = viewModel::onResetClicked,
            resetMessageState = viewModel::resetMessageState,
            onBackPressed = onBackPressed
        )
    }
    configNetworkKeysGraph(
        appState = appState,
        onNavigateToDestination = onNavigateToDestination,
        onBackPressed = onBackPressed
    )
    configApplicationKeysGraph(
        appState = appState,
        onNavigateToDestination = onNavigateToDestination,
        onBackPressed = onBackPressed
    )
    elementsGraph(
        appState = appState,
        onNavigateToDestination = onNavigateToDestination,
        onBackPressed = onBackPressed
    )
}

