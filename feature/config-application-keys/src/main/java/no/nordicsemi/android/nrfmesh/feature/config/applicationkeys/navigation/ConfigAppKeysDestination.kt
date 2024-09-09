package no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.navigation

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination.Companion.ARG
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeysDestination
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.applicationKeysGraph
import no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.ConfigAppKeysRoute
import no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.ConfigAppKeysViewModel
import java.util.UUID

object ConfigAppKeysDestination : MeshNavigationDestination {
    override val route: String = "config_app_key_route/{$ARG}"
    override val destination: String = "config_app_key_destination"

    /**
     * Creates destination route for a network key index.
     *
     * @param uuid UUID of the node to which the key is added
     */
    fun createNavigationRoute(uuid: UUID): String =
        "config_app_key_route/${Uri.encode(uuid.toString())}"
}

fun NavGraphBuilder.configApplicationKeysGraph(
    appState: AppState,
    onNavigateToDestination: (MeshNavigationDestination, String) -> Unit,
    onBackPressed: () -> Unit
) {
    composable(route = ConfigAppKeysDestination.route) {
        val viewmodel = hiltViewModel<ConfigAppKeysViewModel>()
        val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
        ConfigAppKeysRoute(
            appState = appState,
            uiState = uiState,
            navigateToNetworkKeys = {
                onNavigateToDestination(
                    ApplicationKeysDestination,
                    ApplicationKeysDestination.route
                )
            },
            onAddKeyClicked = viewmodel::addApplicationKey,
            onSwiped = viewmodel::onSwiped,
            onRefresh = viewmodel::onRefresh,
            resetMessageState = viewmodel::resetMessageState,
            onBackPressed = onBackPressed,
        )
    }
    applicationKeysGraph(
        appState = appState,
        onNavigateToKey = onNavigateToDestination,
        onBackPressed = onBackPressed
    )
}

