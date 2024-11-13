package no.nordicsemi.android.nrfmesh.feature.model.navigation

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination.Companion.ARG
import no.nordicsemi.android.nrfmesh.feature.model.ModelRoute
import no.nordicsemi.android.nrfmesh.feature.model.ModelViewModel
import no.nordicsemi.kotlin.mesh.core.model.Address

object ModelDestination : MeshNavigationDestination {
    override val route: String = "model_route/{$ARG}"
    override val destination: String = "model_destination"

    /**
     * Creates destination route for a network key index.
     *
     * @param address Address of the parent element.
     * @return The route string.
     */
    @OptIn(ExperimentalStdlibApi::class)
    fun createNavigationRoute(address: Address): String =
        "model_route/${Uri.encode(address.toHexString())}"
}

fun NavGraphBuilder.modelGraph(
    appState: AppState,
    onBackPressed: () -> Unit
) {
    composable(route = ModelDestination.route) {
        val viewModel = hiltViewModel<ModelViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ModelRoute(
            appState = appState,
            uiState = uiState,
            send = viewModel::send,
            requestNodeIdentityStates = viewModel::requestNodeIdentityStates,
            resetMessageState = viewModel::resetMessageState,
            onBackPressed = onBackPressed
        )
    }
}