package no.nordicsemi.android.nrfmesh.feature.application.keys.navigation

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination.Companion.ARG
import no.nordicsemi.android.nrfmesh.feature.application.keys.ApplicationKeyRoute
import no.nordicsemi.android.nrfmesh.feature.application.keys.ApplicationKeyViewModel
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex

object ApplicationKeyDestination : MeshNavigationDestination {
    override val route: String = "application_key_route/{$ARG}"
    override val destination: String = "application_key_destination"

    /**
     * Creates destination route for a application key index.
     */
    fun createNavigationRoute(appKeyIndexArg: KeyIndex): String =
        "application_key_route/${Uri.encode(appKeyIndexArg.toInt().toString())}"

    /**
     * Returns the application key index from a [NavBackStackEntry] after a topic destination
     * navigation call.
     */
    fun fromNavArgs(entry: NavBackStackEntry): String {
        val encodedId = entry.arguments?.getString(ARG)!!
        return Uri.decode(encodedId)
    }
}

internal fun NavGraphBuilder.applicationKeyGraph(
    appState: AppState,
    onBackPressed: () -> Unit
) {
    composable(route = ApplicationKeyDestination.route) {
        val viewmodel = hiltViewModel<ApplicationKeyViewModel>()
        val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
        ApplicationKeyRoute(
            appState = appState,
            uiState = uiState,
            onNameChanged = viewmodel::onNameChanged,
            onKeyChanged = viewmodel::onKeyChanged,
            onBoundNetworkKeyChanged = viewmodel::onBoundNetworkKeyChanged,
            onBackPressed = onBackPressed
        )
    }
}