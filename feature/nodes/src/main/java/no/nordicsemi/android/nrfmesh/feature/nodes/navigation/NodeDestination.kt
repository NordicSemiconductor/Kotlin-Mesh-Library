package no.nordicsemi.android.nrfmesh.feature.nodes.navigation

import android.net.Uri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.feature.config.networkkeys.navigation.ConfigNetworkKeyDestination
import no.nordicsemi.android.feature.config.networkkeys.navigation.configNetworkKeysGraph
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination.Companion.ARG
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

    /**
     * Returns the topicId from a [NavBackStackEntry] after a topic destination navigation call
     */
    fun fromNavArgs(entry: NavBackStackEntry): String {
        val encodedId = entry.arguments?.getString(ARG)!!
        return Uri.decode(encodedId)
    }
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
                    ConfigNetworkKeyDestination,
                    ConfigNetworkKeyDestination.createNavigationRoute(it)
                )
            },
            onApplicationKeysClicked = {
                /*onNavigateToDestination(
                    ConfigNetworkKeyDestination,
                    ConfigNetworkKeyDestination.createNavigationRoute(it)
                )*/
            },
            onElementsClicked = {
                /*onNavigateToDestination(
                    ConfigNetworkKeyDestination,
                    ConfigNetworkKeyDestination.createNavigationRoute(it)
                )*/
            },
            onGetTtlClicked = { /*TODO*/ },
            onProxyStateToggled = viewModel::onProxyStateToggled,
            onGetProxyStateClicked = viewModel::onGetProxyStateClicked,
            onExcluded = viewModel::onExcluded,
            onResetClicked = viewModel::onResetClicked,
            onBackPressed = onBackPressed
        )
    }
    configNetworkKeysGraph(
        appState = appState,
        navigateToNetworkKeys = {
            onNavigateToDestination(ConfigNetworkKeyDestination, ConfigNetworkKeyDestination.route)
        },
        onBackPressed = onBackPressed
    )
}

