package no.nordicsemi.android.nrfmesh.feature.application.keys.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.application.keys.ApplicationKeysRoute
import no.nordicsemi.android.nrfmesh.feature.application.keys.ApplicationKeysViewModel
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex

object ApplicationKeysDestination : MeshNavigationDestination {
    override val route: String = "application_keys_route"
    override val destination: String = "application_keys_destination"
}

fun NavGraphBuilder.applicationKeysGraph(
    onBackPressed: () -> Unit,
    onNavigateToApplicationKey: (KeyIndex) -> Unit
) {
    composable(route = ApplicationKeysDestination.route) {
        val viewModel = hiltViewModel<ApplicationKeysViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        ApplicationKeysRoute(
            uiState = uiState,
            navigateToKey = onNavigateToApplicationKey,
            onAddKeyClicked = viewModel::addApplicationKey,
            onSwiped = viewModel::onSwiped,
            onUndoClicked = viewModel::onUndoSwipe,
            remove = viewModel::remove
        )
    }
    applicationKeyGraph(onBackPressed = onBackPressed)
}