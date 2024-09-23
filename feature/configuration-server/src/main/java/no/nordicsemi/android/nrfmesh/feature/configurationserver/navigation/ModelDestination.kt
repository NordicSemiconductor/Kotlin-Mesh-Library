package no.nordicsemi.android.nrfmesh.feature.configurationserver.navigation

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination.Companion.ARG
import no.nordicsemi.android.nrfmesh.feature.configurationserver.ModelRoute
import no.nordicsemi.android.nrfmesh.feature.configurationserver.ModelViewModel
import no.nordicsemi.kotlin.mesh.core.model.Address

object ConfigurationServerDestination : MeshNavigationDestination {
    override val route: String = "configuration_server_route/{$ARG}"
    override val destination: String = "configuration_server_destination"

    /**
     * Creates destination route for a network key index.
     *
     * @param address Address of the parent element.
     * @return The route string.
     */
    @OptIn(ExperimentalStdlibApi::class)
    fun createNavigationRoute(address: Address): String =
        "configuration_server_route/${Uri.encode(address.toHexString())}"
}

fun NavGraphBuilder.configurationServerGraph(
    appState: AppState,
    onNavigateToDestination: (MeshNavigationDestination, String) -> Unit,
    onBackPressed: () -> Unit
) {
    composable(route = ConfigurationServerDestination.route) {
        val viewModel = hiltViewModel<ModelViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ModelRoute(
            appState = appState,
            uiState = uiState,
            send = viewModel::send,
            resetMessageState = viewModel::resetMessageState,
            onBackPressed = onBackPressed
        )
    }
}