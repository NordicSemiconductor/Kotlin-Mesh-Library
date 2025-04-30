package no.nordicsemi.android.nrfmesh.feature.proxy.navigation

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
import no.nordicsemi.android.nrfmesh.feature.proxy.ProxyRoute
import no.nordicsemi.android.nrfmesh.feature.proxy.ProxyViewModel

@Serializable
@Parcelize
data object ProxyRoute : Parcelable

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
            onScanResultSelected = { context, result ->
                viewModel.connect(context = context, result = result)
            },
            send = viewModel::send,
            resetMessageState = viewModel::resetMessageState,
        )
    }
}