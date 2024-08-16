package no.nordicsemi.android.nrfmesh.feature.network.keys.navigation

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination.Companion.ARG
import no.nordicsemi.android.nrfmesh.feature.network.keys.NetworkKeyRoute
import no.nordicsemi.android.nrfmesh.feature.network.keys.NetworkKeyViewModel
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex

object NetworkKeyDestination : MeshNavigationDestination {
    override val route: String = "network_key_route/{$ARG}"
    override val destination: String = "network_key_destination"

    /**
     * Creates destination route for a network key index.
     */
    fun createNavigationRoute(netKeyIndexArg: KeyIndex): String =
        "network_key_route/${Uri.encode(netKeyIndexArg.toInt().toString())}"

    /**
     * Returns the topicId from a [NavBackStackEntry] after a topic destination navigation call
     */
    fun fromNavArgs(entry: NavBackStackEntry): String {
        val encodedId = entry.arguments?.getString(ARG)!!
        return Uri.decode(encodedId)
    }
}

internal fun NavGraphBuilder.networkKeyGraph(onBackPressed: () -> Unit) {
    composable(route = NetworkKeyDestination.route) {
        val viewModel = hiltViewModel<NetworkKeyViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        NetworkKeyRoute(
            uiState = uiState,
            onNameChanged = viewModel::onNameChanged,
            onKeyChanged = viewModel::onKeyChanged,
        )
    }
}

