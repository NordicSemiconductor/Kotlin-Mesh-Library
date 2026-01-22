package no.nordicsemi.android.nrfmesh.feature.network.keys.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.Navigator
import no.nordicsemi.android.nrfmesh.feature.network.keys.NetworkKeysScreen
import no.nordicsemi.android.nrfmesh.feature.network.keys.NetworkKeysViewModel
import no.nordicsemi.android.nrfmesh.feature.network.keys.key.navigation.NetworkKeyContentKey
import no.nordicsemi.android.nrfmesh.feature.network.keys.key.navigation.networkKeyEntry
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex

@Serializable
data object NetworkKeysContentKey : NavKey

@Composable
fun NetworkKeysScreenRoute(
    snackbarHostState: SnackbarHostState,
    highlightSelectedItem: Boolean,
    onNetworkKeyClicked: (KeyIndex) -> Unit,
    navigateToKey: (KeyIndex) -> Unit,
    navigateUp: () -> Unit,
) {
    // val viewModel = hiltViewModel<NetworkKeyViewModel>()
    // val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // NetworkKeysRoute(
    //     snackbarHostState = snackbarHostState,
    //     highlightSelectedItem = highlightSelectedItem,
    //     selectedKeyIndex = uiState.selectedKeyIndex,
    //     keys = uiState.keys,
    //     onAddKeyClicked = viewModel::addNetworkKey,
    //     onNetworkKeyClicked = {
    //         viewModel.selectKeyIndex(keyIndex = it)
    //         onNetworkKeyClicked(it)
    //     },
    //     navigateToKey = {
    //         viewModel.selectKeyIndex(keyIndex = it)
    //         navigateToKey(it)
    //     },
    //     onSwiped = {
    //         viewModel.onSwiped(it)
    //         if (uiState.selectedKeyIndex == it.index) {
    //             navigateUp()
    //         }
    //     },
    //     onUndoClicked = viewModel::onUndoSwipe,
    //     remove = viewModel::remove
    // )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.networkKeysEntry(appState: AppState, navigator: Navigator) {
    entry<NetworkKeysContentKey>(
        metadata = ListDetailSceneStrategy.detailPane()
    ) {
        val viewModel = hiltViewModel<NetworkKeysViewModel, NetworkKeysViewModel.Factory>(
            key = "NetworkKeysViewModel"
        ) { factory ->
            factory.create()
        }
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
                // onNetworkKeyClicked(it)
            },
            navigateToKey = {
                viewModel.selectKeyIndex(keyIndex = it)
                navigator.navigate(key = NetworkKeyContentKey(keyIndex = it))
                // navigateToKey(it)
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