package no.nordicsemi.android.nrfmesh.feature.application.keys.navigation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.application.keys.ApplicationKeysRoute
import no.nordicsemi.android.nrfmesh.feature.application.keys.ApplicationKeysViewModel
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex

@Serializable
@Parcelize
data object ApplicationKeysRoute : Parcelable

object ApplicationKeysDestination : MeshNavigationDestination {
    override val route: String = "application_keys_route"
    override val destination: String = "application_keys_destination"
}

fun NavGraphBuilder.applicationKeysGraph(
    appState: AppState,
    onNavigateToKey: (MeshNavigationDestination, String) -> Unit,
    onBackPressed: () -> Unit,
) {
}

@Composable
fun ApplicationsKeysScreenRoute(
    highlightSelectedItem: Boolean,
    navigateToKey: (KeyIndex) -> Unit,
    navigateUp: () -> Unit
) {
    val viewModel = hiltViewModel<ApplicationKeysViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ApplicationKeysRoute(
        highlightSelectedItem = highlightSelectedItem,
        keys = uiState.keys,
        onAddKeyClicked = viewModel::addApplicationKey,
        navigateToKey = {
            viewModel.selectKeyIndex(it)
            navigateToKey(it)
        },
        onSwiped = {
            viewModel.onSwiped(it)
            if(viewModel.isCurrentlySelectedKey(it.index)) {
                navigateUp()
            }
        },
        onUndoClicked = viewModel::onUndoSwipe,
        remove = { viewModel.remove(it) }
    )
}