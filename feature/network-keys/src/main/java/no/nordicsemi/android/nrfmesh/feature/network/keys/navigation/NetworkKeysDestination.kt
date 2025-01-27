package no.nordicsemi.android.nrfmesh.feature.network.keys.navigation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.network.keys.NetworkKeysRoute
import no.nordicsemi.android.nrfmesh.feature.network.keys.NetworkKeysViewModel
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex

@Parcelize
data object NetworkKeysRoute : Parcelable

object NetworkKeysDestination : MeshNavigationDestination {
    override val route: String = "network_keys_route"
    override val destination: String = "network_keys_destination"
}

@Composable
fun NetworkKeysScreenRoute(
    highlightSelectedItem: Boolean,
    navigateToKey: (KeyIndex) -> Unit,
    navigateUp: () -> Unit
) {
    val viewModel = hiltViewModel<NetworkKeysViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NetworkKeysRoute(
        highlightSelectedItem = highlightSelectedItem,
        networkKeys = uiState.keys,
        onAddKeyClicked = viewModel::addNetworkKey,
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
        remove = viewModel::remove
    )
}