package no.nordicsemi.android.nrfmesh.feature.proxy.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.proxy.ProxyRoute
import no.nordicsemi.android.nrfmesh.feature.proxy.viewmodel.ProxyViewModel

@Serializable
data object ProxyRoute

object ProxyDestination : MeshNavigationDestination {
    override val route: String = "proxy_route"
    override val destination: String = "proxy_destination"
}

const val PROXY_ROUTE = "proxy_route"

fun NavController.navigateToProxy(navOptions: NavOptions? = null) = navigate(
    route = ProxyDestination.route,
    navOptions = navOptions
)

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