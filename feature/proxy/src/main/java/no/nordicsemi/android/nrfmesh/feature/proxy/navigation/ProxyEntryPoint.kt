package no.nordicsemi.android.nrfmesh.feature.proxy.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import no.nordicsemi.android.nrfmesh.core.navigation.ProxyKey
import no.nordicsemi.android.nrfmesh.feature.proxy.ProxyScreen
import no.nordicsemi.android.nrfmesh.feature.proxy.ProxyViewModel

fun EntryProviderScope<NavKey>.proxyEntry() {
    entry<ProxyKey> {
        val viewModel = hiltViewModel<ProxyViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ProxyScreen(
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