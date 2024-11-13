package no.nordicsemi.android.nrfmesh.feature.proxy.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.proxy.ProxyRoute
import no.nordicsemi.android.nrfmesh.feature.proxy.viewmodel.ProxyViewModel

object ProxyDestination : MeshNavigationDestination {
    override val route: String = "proxy_route"
    override val destination: String = "proxy_destination"
}

fun NavGraphBuilder.proxyFilterGraph() {
    composable(route = ProxyDestination.route) {
        val viewModel = hiltViewModel<ProxyViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ProxyRoute(
            uiState = uiState,
            onBluetoothEnabled = {

            },
            onLocationEnabled = {

            },
            onAutoConnectToggled = viewModel::onAutoConnectToggled,
            onDisconnectClicked = viewModel::disconnect,
            onDeviceFound = { _, _ ->

            }
        )
    }
}