package no.nordicsemi.android.nrfmesh.feature.settings.navigation

import android.os.Parcelable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.core.navigation.SettingsItemRoute
import no.nordicsemi.android.nrfmesh.feature.settings.SettingsViewModel

@Serializable
@Parcelize
data object SettingsRoute : Parcelable

object SettingsDestination : MeshNavigationDestination {
    override val route: String = "settings_route"
    override val destination: String = "settings_destination"
}

const val SETTINGS_ROUTE = "settings_route"

fun NavController.navigateToSettings(navOptions: NavOptions) = navigate(
    route = SettingsRoute,
    navOptions = navOptions
)

internal fun NavController.navigateToSettingsItem(
    settingsItemRoute: String, navOptions: NavOptionsBuilder.() -> Unit = {}
) = navigate(
    route = SettingsItemRoute(
        settingsItemRoute
    )
) {
    navOptions()
}

fun NavGraphBuilder.settingsListDetailsScreen(appState: AppState) {
    composable<SettingsRoute> {
        val viewModel = hiltViewModel<SettingsViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        SettingsListDetailsScreen(
            appState = appState,
            uiState = uiState,
            onNameChanged = viewModel::onNameChanged,
            importNetwork = viewModel::importNetwork,
            resetNetwork = viewModel::resetNetwork
        )
    }
}