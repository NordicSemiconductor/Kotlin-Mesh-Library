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
        keys = uiState.keys,
        onAddKeyClicked = viewModel::addApplicationKey,
        onApplicationKeyClicked = {
            viewModel.selectKeyIndex(it)
            onApplicationKeyClicked(it)
        },
        navigateToKey = navigateToKey,
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