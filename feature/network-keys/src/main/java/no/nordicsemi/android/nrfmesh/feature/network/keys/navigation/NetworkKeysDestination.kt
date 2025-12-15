package no.nordicsemi.android.nrfmesh.feature.network.keys.navigation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.feature.network.keys.NetworkKeysRoute
import no.nordicsemi.android.nrfmesh.feature.network.keys.NetworkKeysViewModel
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex

@Serializable
@Parcelize
data object NetworkKeysContent : Parcelable

@Composable
fun NetworkKeysScreenRoute(
    highlightSelectedItem: Boolean,
    onNetworkKeyClicked: (KeyIndex) -> Unit,
    navigateToKey: (KeyIndex) -> Unit,
    navigateUp: () -> Unit
) {
    val viewModel = hiltViewModel<NetworkKeysViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NetworkKeysRoute(
        highlightSelectedItem = highlightSelectedItem,
        selectedKeyIndex = uiState.selectedKeyIndex,
        keys = uiState.keys,
        onAddKeyClicked = viewModel::addNetworkKey,
        onNetworkKeyClicked = {
            viewModel.selectKeyIndex(keyIndex = it)
            onNetworkKeyClicked(it)
        },
        navigateToKey = {
            viewModel.selectKeyIndex(keyIndex = it)
            navigateToKey(it)
        },
        onSwiped = {
            viewModel.onSwiped(it)
            if(uiState.selectedKeyIndex == it.index) {
                navigateUp()
            }
        },
        onUndoClicked = viewModel::onUndoSwipe,
        remove = viewModel::remove
    )
}