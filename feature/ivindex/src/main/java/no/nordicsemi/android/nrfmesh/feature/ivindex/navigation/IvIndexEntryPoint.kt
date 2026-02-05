package no.nordicsemi.android.nrfmesh.feature.ivindex.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.core.navigation.SettingsListDetailSceneKey
import no.nordicsemi.android.nrfmesh.feature.ivindex.IvIndexScreen
import no.nordicsemi.android.nrfmesh.feature.ivindex.IvIndexViewModel

@Serializable
data object IvIndexContentKey : NavKey

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.ivIndexEntry() {
    entry<IvIndexContentKey>(
        metadata = ListDetailSceneStrategy.detailPane(
            sceneKey = SettingsListDetailSceneKey
        )
    ) {
        val viewModel = hiltViewModel<IvIndexViewModel, IvIndexViewModel.Factory>(
            key = "IvIndexViewModel"
        ) { factory ->
            factory.create()
        }
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        IvIndexScreen(
            isIvIndexChangeAllowed = uiState.isIvIndexChangeAllowed,
            ivIndex = uiState.ivIndex,
            onIvIndexChanged = viewModel::onIvIndexChanged,
            onIvIndexTestModeToggled = viewModel::toggleIvUpdateTestMode,
            testMode = uiState.testMode
        )
    }
}