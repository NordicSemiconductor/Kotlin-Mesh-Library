package no.nordicsemi.android.nrfmesh.feature.proxy.destination

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.common.navigation.createSimpleDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.proxy.ProxyRoute
import no.nordicsemi.android.nrfmesh.feature.proxy.viewmodel.ProxyViewModel

val proxy = createSimpleDestination("proxy")

val proxyDestination = defineDestination(proxy) {
    val viewModel: ProxyViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProxyRoute(
        uiState = uiState,
        onBluetoothEnabled = viewModel::onBluetoothEnabled,
        onLocationEnabled = viewModel::onLocationEnabled,
        onAutoConnectChecked = viewModel::onAutomaticConnectionChanged,
        onDisconnectClicked = viewModel::disconnect,
        onDeviceFound = viewModel::connect
    )
}

val proxyDestinations = listOf(proxyDestination)