@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.application.keys.destinations

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.application.keys.ApplicationKeyRoute
import no.nordicsemi.android.nrfmesh.feature.application.keys.ApplicationKeyViewModel
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex

object ApplicationKeyDestination : MeshNavigationDestination {
    const val appKeyIndexArg = "appKeyIndexArg"
    override val route: String = "application_key_route/{$appKeyIndexArg}"
    override val destination: String = "application_key_destination"

    /**
     * Creates destination route for a application key index.
     */
    fun createNavigationRoute(netKeyIndexArg: KeyIndex): String =
        "application_key_route/${Uri.encode(netKeyIndexArg.toInt().toString())}"

    /**
     * Returns the topicId from a [NavBackStackEntry] after a topic destination navigation call
     */
    fun fromNavArgs(entry: NavBackStackEntry): String {
        val encodedId = entry.arguments?.getString(appKeyIndexArg)!!
        return Uri.decode(encodedId)
    }
}

internal fun NavGraphBuilder.applicationKeyGraph(onBackPressed: () -> Unit) {
    composable(route = ApplicationKeyDestination.route) {
        val viewModel = hiltViewModel<ApplicationKeyViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ApplicationKeyRoute(
            uiState = uiState,
            onNameChanged = viewModel::onNameChanged,
            onKeyChanged = viewModel::onKeyChanged,
            onBoundNetworkKeyChanged = viewModel::onBoundNetworkKeyChanged,
        )
    }
}
