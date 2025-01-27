package no.nordicsemi.android.nrfmesh.feature.settings.navigation

import android.os.Parcelable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.feature.settings.SettingsViewModel

@Serializable
@Parcelize
data object SettingsRoute : Parcelable

@Serializable
@Parcelize
data object SettingsBaseRoute : Parcelable

fun NavController.navigateToSettings(navOptions: NavOptions) = navigate(
    route = SettingsRoute,
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
            importNetwork = viewModel::importNetwork,
            resetNetwork = viewModel::resetNetwork,
            save = viewModel::save
        )
    }
}