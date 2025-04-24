package no.nordicsemi.android.nrfmesh.feature.settings.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.core.navigation.ClickableSetting
import no.nordicsemi.android.nrfmesh.feature.settings.SettingsViewModel

@Serializable
data class SettingsRoute(val selectedSetting: ClickableSetting? = null)

fun NavController.navigateToSettings(
    listItem: ClickableSetting? = null,
    navOptions: NavOptions? = null,
) = navigate(
    route = SettingsRoute(selectedSetting = listItem),
    navOptions = navOptions
)

fun NavGraphBuilder.settingsListDetailsScreen() {
    composable<SettingsRoute> {
        val viewModel = hiltViewModel<SettingsViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        SettingsListDetailsScreen(
            uiState = uiState,
            onItemSelected = viewModel::onItemSelected,
            onNameChanged = viewModel::onNameChanged,
            moveProvisioner = viewModel::moveProvisioner,
            save = viewModel::save
        )
    }
}