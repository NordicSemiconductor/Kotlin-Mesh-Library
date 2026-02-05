package no.nordicsemi.android.nrfmesh.feature.network.keys.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.Navigator
import no.nordicsemi.android.nrfmesh.core.navigation.SettingsListDetailSceneKey
import no.nordicsemi.android.nrfmesh.feature.network.keys.NetworkKeysScreen
import no.nordicsemi.android.nrfmesh.feature.network.keys.NetworkKeysViewModel
import no.nordicsemi.android.nrfmesh.feature.network.keys.key.navigation.NetworkKeyContentKey
import no.nordicsemi.android.nrfmesh.feature.network.keys.key.navigation.networkKeyEntry

@Serializable
data object NetworkKeysContentKey : NavKey

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.networkKeysEntry(appState: AppState, navigator: Navigator) {
    entry<NetworkKeysContentKey>(
        metadata = ListDetailSceneStrategy.detailPane(
            sceneKey = SettingsListDetailSceneKey
        )
    ) {
        val viewModel = hiltViewModel<NetworkKeysViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        NetworkKeysScreen(
            snackbarHostState = appState.snackbarHostState,
            highlightSelectedItem = false,
            selectedKeyIndex = uiState.selectedKeyIndex,
            keys = uiState.keys,
            onAddKeyClicked = viewModel::addNetworkKey,
            onNetworkKeyClicked = {
                viewModel.selectKeyIndex(keyIndex = it)
                navigator.navigate(key = NetworkKeyContentKey(keyIndex = it))
            },
            navigateToKey = {
                viewModel.selectKeyIndex(keyIndex = it)
                navigator.navigate(key = NetworkKeyContentKey(keyIndex = it))
            },
            onSwiped = {
                viewModel.onSwiped(it)
                if (uiState.selectedKeyIndex == it.index) {
                    navigator.goBack()
                }
            },
            onUndoClicked = viewModel::onUndoSwipe,
            remove = viewModel::remove
        )
    }
    networkKeyEntry(appState = appState, navigator = navigator)
}