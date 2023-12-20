@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.feature.proxy

import android.content.Context
import android.os.ParcelUuid
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.permissions.ble.RequireBluetooth
import no.nordicsemi.android.common.permissions.ble.RequireLocation
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResults
import no.nordicsemi.android.kotlin.ble.ui.scanner.ScannerView
import no.nordicsemi.android.kotlin.ble.ui.scanner.main.DeviceListItem
import no.nordicsemi.android.kotlin.mesh.bearer.android.utils.MeshProxyService
import no.nordicsemi.android.nrfmesh.core.data.NetworkConnectionState
import no.nordicsemi.android.nrfmesh.core.data.ProxyState
import no.nordicsemi.android.nrfmesh.core.ui.BottomSheetTopAppBar
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.feature.proxy.viewmodel.ProxyScreenUiState

@Composable
internal fun ProxyRoute(
    uiState: ProxyScreenUiState,
    onBluetoothEnabled: (Boolean) -> Unit,
    onLocationEnabled: (Boolean) -> Unit,
    onAutoConnectToggled: (Boolean) -> Unit,
    onDeviceFound: (Context, BleScanResults) -> Unit,
    onDisconnectClicked: () -> Unit,
) {
    ProxyFilterScreen(
        onBluetoothEnabled = onBluetoothEnabled,
        onLocationEnabled = onLocationEnabled,
        proxyState = uiState.proxyState,
        onAutoConnectToggled = onAutoConnectToggled,
        onDisconnectClicked = onDisconnectClicked,
        onDeviceFound = onDeviceFound
    )
}

@Composable
private fun ProxyFilterScreen(
    onBluetoothEnabled: (Boolean) -> Unit,
    onLocationEnabled: (Boolean) -> Unit,
    proxyState: ProxyState,
    onAutoConnectToggled: (Boolean) -> Unit,
    onDisconnectClicked: () -> Unit,
    onDeviceFound: (Context, BleScanResults) -> Unit
) {
    val scope = rememberCoroutineScope()
    var showProxyScannerSheet by rememberSaveable { mutableStateOf(false) }
    val proxyScannerSheetState = rememberModalBottomSheetState()
    RequireBluetooth(onChanged = onBluetoothEnabled) {
        RequireLocation(onChanged = onLocationEnabled) {
            LazyColumn {
                proxyFilterInfo(
                    proxyState = proxyState,
                    onAutoConnectToggled = onAutoConnectToggled,
                    onConnectClicked = { showProxyScannerSheet = true },
                    onDisconnectClicked = onDisconnectClicked
                )
            }
            if (showProxyScannerSheet)
                ModalBottomSheet(
                    onDismissRequest = { showProxyScannerSheet = false },
                    sheetState = proxyScannerSheetState
                ) {
                    BottomSheetTopAppBar(
                        navigationIcon = Icons.Rounded.Close,
                        onNavigationIconClick = {
                            scope.launch {
                                proxyScannerSheetState.hide()
                                delay(1000)
                                showProxyScannerSheet = false
                            }
                        },
                        title = "Proxies",
                        titleStyle = MaterialTheme.typography.titleLarge
                    )
                    ScannerSection(
                        onDeviceFound = { context, results ->
                            showProxyScannerSheet = false
                            onDeviceFound(context, results)
                        }
                    )
                }
        }
    }
}

private fun LazyListScope.proxyFilterInfo(
    proxyState: ProxyState,
    onAutoConnectToggled: (Boolean) -> Unit,
    onConnectClicked: () -> Unit,
    onDisconnectClicked: () -> Unit
) {
    item {
        AutomaticConnectionRow(
            proxyState = proxyState,
            onAutoConnectToggled = onAutoConnectToggled,
            onConnectClicked = onConnectClicked,
            onDisconnectClicked = onDisconnectClicked
        )
    }
}

@Composable
private fun AutomaticConnectionRow(
    proxyState: ProxyState,
    onAutoConnectToggled: (Boolean) -> Unit,
    onConnectClicked: () -> Unit,
    onDisconnectClicked: () -> Unit
) {
    ElevatedCardItem(
        modifier = Modifier.padding(start = 8.dp, top = 8.dp, end = 8.dp),
        imageVector = Icons.Outlined.AutoAwesome,
        title = stringResource(R.string.label_automatic_connection),
        titleAction = {
            Switch(
                modifier = Modifier.padding(start = 16.dp),
                checked = proxyState.autoConnect,
                onCheckedChange = {
                    onAutoConnectToggled(it)
                }
            )
        },
        subtitle = proxyState.connectionState.describe(),
        supportingText = stringResource(R.string.label_automatic_connection_rationale),
        actions = {
            OutlinedButton(onClick = onConnectClicked, enabled = !proxyState.autoConnect) {
                Text(text = "Connect")
            }
            Spacer(modifier = Modifier.size(16.dp))
            OutlinedButton(onClick = {
                onAutoConnectToggled(false)
                onDisconnectClicked()
            }) {
                Text(text = "Disconnect")
            }
        }
    )
}

@Composable
private fun ScannerSection(
    onDeviceFound: (Context, BleScanResults) -> Unit
) {
    val context = LocalContext.current
    ScannerView(
        uuid = ParcelUuid(MeshProxyService.uuid),
        onResult = { onDeviceFound(context, it) },
        deviceItem = {
            DeviceListItem(
                modifier = Modifier.padding(vertical = 16.dp),
                name = it.device.name,
                address = it.device.address
            )
        },
        showFilter = false
    )
}

@Composable
private fun NetworkConnectionState.describe() = when (this) {
    NetworkConnectionState.Scanning -> stringResource(R.string.label_scanning)
    is NetworkConnectionState.Connecting -> stringResource(
        R.string.label_connecting_to, device.name ?: R.string.label_unknown
    )

    is NetworkConnectionState.Connected -> stringResource(
        R.string.label_connected_to, device.name ?: R.string.label_unknown
    )

    NetworkConnectionState.Disconnected -> stringResource(R.string.label_proxy_disconnected)
}