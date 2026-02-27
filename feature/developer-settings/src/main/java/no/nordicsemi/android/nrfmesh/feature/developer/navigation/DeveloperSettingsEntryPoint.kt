package no.nordicsemi.android.nrfmesh.feature.developer.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.SettingsListDetailSceneKey
import no.nordicsemi.android.nrfmesh.feature.developer.DeveloperSettingsScreen
import no.nordicsemi.android.nrfmesh.feature.developer.DeveloperSettingsViewModel

@Serializable
data object DeveloperSettingsContentKey : NavKey

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.developerSettingsEntry(appState: AppState) {
    entry<DeveloperSettingsContentKey>(
        metadata = ListDetailSceneStrategy.detailPane(
            sceneKey = SettingsListDetailSceneKey
        )
    ) {
        val viewModel = hiltViewModel<DeveloperSettingsViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        DeveloperSettingsScreen(
            developerSettings = uiState.settings,
            onQuickProvisioningEnabled = viewModel::onQuickProvisioningEnabled,
            onAlwaysReconfigure = viewModel::onAlwaysReconfigure
        )
    }
}