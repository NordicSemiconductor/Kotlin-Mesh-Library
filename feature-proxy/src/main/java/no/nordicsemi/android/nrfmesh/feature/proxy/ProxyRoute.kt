@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.feature.proxy

import android.content.Context
import android.os.ParcelUuid
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Switch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BluetoothSearching
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResults
import no.nordicsemi.android.kotlin.ble.ui.scanner.ScannerView
import no.nordicsemi.android.kotlin.ble.ui.scanner.main.DeviceListItem
import no.nordicsemi.android.kotlin.mesh.bearer.android.utils.MeshProxyService
import no.nordicsemi.android.nrfmesh.core.ui.BottomSheetTopAppBar
import no.nordicsemi.android.nrfmesh.core.ui.MeshTwoLineListItem
import no.nordicsemi.android.nrfmesh.feature.proxy.viewmodel.ProxyScreenUiState
import no.nordicsemi.android.nrfmesh.feature.proxy.viewmodel.ProxyState

@Composable
internal fun ProxyRoute(
    uiState: ProxyScreenUiState,
    onAutoConnectChecked: (Boolean) -> Unit,
    onDeviceFound: (Context, BleScanResults) -> Unit,
) {
    ProxyFilterScreen(
        autoConnect = uiState.autoConnect,
        onAutoConnectChecked = onAutoConnectChecked,
        proxyState = uiState.proxyState,
        onDeviceFound = onDeviceFound
    )
}

@Composable
private fun ProxyFilterScreen(
    autoConnect: Boolean,
    onAutoConnectChecked: (Boolean) -> Unit,
    proxyState: ProxyState,
    onDeviceFound: (Context, BleScanResults) -> Unit
) {
    val scope = rememberCoroutineScope()
    var showProxyScannerSheet by rememberSaveable { mutableStateOf(false) }
    val proxyScannerSheetState = rememberModalBottomSheetState()

    LazyColumn {
        proxyFilterInfo(
            autoConnect = autoConnect,
            onAutoConnectChecked = onAutoConnectChecked,
            proxyState = proxyState,
            onProxyRowClicked = { showProxyScannerSheet = true }
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

private fun LazyListScope.proxyFilterInfo(
    autoConnect: Boolean,
    onAutoConnectChecked: (Boolean) -> Unit,
    proxyState: ProxyState,
    onProxyRowClicked: () -> Unit
) {
    item {
        AutomaticConnectionRow(
            autoConnect = autoConnect,
            onAutoConnectChecked = onAutoConnectChecked
        )
    }
    item {
        ProxyRow(
            autoConnect = autoConnect,
            proxyState = proxyState,
            onProxyRowClicked = onProxyRowClicked
        )
    }
}

@Composable
private fun AutomaticConnectionRow(
    autoConnect: Boolean,
    onAutoConnectChecked: (Boolean) -> Unit,
) {
    MeshTwoLineListItem(
        modifier = Modifier.padding(end = 16.dp),
        leadingComposable = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        title = stringResource(R.string.label_automatic_connection),
        trailingComposable = {
            Switch(
                modifier = Modifier.clickable { onAutoConnectChecked(!autoConnect) },
                checked = autoConnect,
                onCheckedChange = null
            )
        }
    )
}

@Composable
private fun ProxyRow(
    autoConnect: Boolean,
    proxyState: ProxyState,
    onProxyRowClicked: () -> Unit
) {
    MeshTwoLineListItem(
        modifier = Modifier
            .padding(end = 16.dp)
            .clickable(enabled = !autoConnect, onClick = onProxyRowClicked),
        leadingComposable = {
            Icon(
                modifier = Modifier.padding(all = 16.dp),
                imageVector = Icons.Outlined.Hub,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        title = stringResource(R.string.title_proxy),
        subtitle = proxyState.describe(),
        trailingComposable = {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Divider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                )
                Spacer(modifier = Modifier.size(16.dp))
                IconButton(enabled = !autoConnect, onClick = onProxyRowClicked) {
                    Icon(
                        imageVector = Icons.Outlined.BluetoothSearching,
                        contentDescription = null,
                        tint = LocalContentColor.current.copy(alpha = 0.6f)
                    )
                }
            }
        }
    )
}

@Composable
private fun ScannerSection(onDeviceFound: (Context, BleScanResults) -> Unit) {
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
private fun ProxyState.describe() = when (this) {
    is ProxyState.Connecting -> stringResource(
        R.string.label_connecting_to, device.name ?: R.string.label_unknown
    )

    is ProxyState.Connected -> stringResource(
        R.string.label_connected_to, device.name ?: R.string.label_unknown
    )
    ProxyState.Scanning, ProxyState.Disconnected -> stringResource(R.string.empty)
}