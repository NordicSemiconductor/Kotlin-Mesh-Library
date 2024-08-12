package no.nordicsemi.android.nrfmesh.destinations

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.ui.provisioning.NetKeySelectorRoute
import no.nordicsemi.android.nrfmesh.viewmodel.NetKeySelectorViewModel
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex

object NetKeySelectorDestination : MeshNavigationDestination {
    const val netKeyIndexArg = "netKeyIndexArg"
    override val route: String = "net_key_selector_route/{$netKeyIndexArg}"
    override val destination: String = "net_key_selector_destination"

    /**
     * Creates destination route for a application key index.
     */
    fun createNavigationRoute(netKeyIndexArg: KeyIndex): String =
        "application_key_route/${Uri.encode(netKeyIndexArg.toInt().toString())}"

    /**
     * Returns the topicId from a [NavBackStackEntry] after a topic destination navigation call
     */
    fun fromNavArgs(entry: NavBackStackEntry): String {
        val encodedId = entry.arguments?.getString(netKeyIndexArg)!!
        return Uri.decode(encodedId)
    }
}

internal fun NavGraphBuilder.netKeySelectorGraph(onBackPressed: (KeyIndex) -> Unit) {
    composable(route = NetKeySelectorDestination.route) {
        val viewModel = hiltViewModel<NetKeySelectorViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        NetKeySelectorRoute(
            uiState = uiState,
            onKeySelected = viewModel::onKeySelected,
            onBackPressed = onBackPressed
        )
    }
}