package no.nordicsemi.android.nrfmesh.feature.application.keys.navigation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.feature.application.keys.ApplicationKeysRoute
import no.nordicsemi.android.nrfmesh.feature.application.keys.ApplicationKeysViewModel
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex

@Serializable
@Parcelize
data object ApplicationKeysContent : Parcelable

@Composable
fun ApplicationKeysScreenRoute(
    highlightSelectedItem: Boolean,
    onApplicationKeyClicked: (KeyIndex) -> Unit,
    navigateToKey: (KeyIndex) -> Unit,
    navigateUp: () -> Unit
) {
    val viewModel = hiltViewModel<ApplicationKeysViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ApplicationKeysRoute(
        highlightSelectedItem = highlightSelectedItem,
        selectedKeyIndex = uiState.selectedKeyIndex,
        keys = uiState.keys,
        onAddKeyClicked = viewModel::addApplicationKey,
        onApplicationKeyClicked = {
            viewModel.selectKeyIndex(keyIndex = it)
            onApplicationKeyClicked(it)
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