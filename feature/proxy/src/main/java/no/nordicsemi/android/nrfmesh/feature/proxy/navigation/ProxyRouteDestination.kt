package no.nordicsemi.android.nrfmesh.feature.proxy.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import no.nordicsemi.android.nrfmesh.core.navigation.Navigator
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.ProxyKey
import no.nordicsemi.android.nrfmesh.feature.proxy.ProxyRoute
import no.nordicsemi.android.nrfmesh.feature.proxy.ProxyViewModel

@Serializable
object ProxyRoute : NavKey

fun NavController.navigateToProxy(navOptions: NavOptions? = null) = navigate(
    route = ProxyRoute,
    navOptions = navOptions
)

fun NavGraphBuilder.proxyFilterGraph() {
    composable<ProxyRoute> {
        val viewModel = hiltViewModel<ProxyViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ProxyRoute(
            uiState = uiState,
            onBluetoothEnabled = viewModel::onBluetoothEnabled,
            onLocationEnabled = viewModel::onLocationEnabled,
            onAutoConnectToggled = viewModel::onAutoConnectToggled,
            onDisconnectClicked = viewModel::disconnect,
            onScanResultSelected = viewModel::connect,
            send = viewModel::send,
            resetMessageState = viewModel::resetMessageState,
        )
    }
}

fun EntryProviderScope<NavKey>.proxyEntry(appState: AppState, navigator: Navigator) {
    entry<ProxyKey> {
        val viewModel = hiltViewModel<ProxyViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ProxyRoute(
            uiState = uiState,
            onBluetoothEnabled = viewModel::onBluetoothEnabled,
            onLocationEnabled = viewModel::onLocationEnabled,
            onAutoConnectToggled = viewModel::onAutoConnectToggled,
            onDisconnectClicked = viewModel::disconnect,
            onScanResultSelected = viewModel::connect,
            send = viewModel::send,
            resetMessageState = viewModel::resetMessageState,
        )
    }
}