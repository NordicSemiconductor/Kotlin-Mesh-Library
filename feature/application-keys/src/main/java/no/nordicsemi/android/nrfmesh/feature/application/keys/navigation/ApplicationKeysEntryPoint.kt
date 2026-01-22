package no.nordicsemi.android.nrfmesh.feature.application.keys.navigation

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
import no.nordicsemi.android.nrfmesh.feature.application.keys.ApplicationKeysScreen
import no.nordicsemi.android.nrfmesh.feature.application.keys.ApplicationKeysViewModel
import no.nordicsemi.android.nrfmesh.feature.application.keys.key.navigation.ApplicationKeyContentKey
import no.nordicsemi.android.nrfmesh.feature.application.keys.key.navigation.applicationKeyEntry

@Serializable
data object ApplicationKeysContentKey : NavKey

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.applicationKeysEntry(appState: AppState, navigator: Navigator) {
    entry<ApplicationKeysContentKey>(
        metadata = ListDetailSceneStrategy.detailPane()
    ) {
        val viewModel = hiltViewModel<ApplicationKeysViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ApplicationKeysScreen(
            snackbarHostState = appState.snackbarHostState,
            highlightSelectedItem = false,
            selectedKeyIndex = uiState.selectedKeyIndex,
            keys = uiState.keys,
            onAddKeyClicked = viewModel::addApplicationKey,
            onApplicationKeyClicked = {
                viewModel.selectKeyIndex(keyIndex = it)
                navigator.navigate(ApplicationKeyContentKey(keyIndex = it))
            },
            navigateToKey = {
                viewModel.selectKeyIndex(keyIndex = it)
                navigator.navigate(ApplicationKeyContentKey(keyIndex = it))
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
    applicationKeyEntry(appState = appState, navigator = navigator)
}