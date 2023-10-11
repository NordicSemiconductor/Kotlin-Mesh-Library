@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.feature.proxy

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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResults
import no.nordicsemi.android.kotlin.ble.ui.scanner.ScannerView
import no.nordicsemi.android.kotlin.ble.ui.scanner.main.DeviceListItem
import no.nordicsemi.android.kotlin.mesh.bearer.android.utils.MeshProxyService
import no.nordicsemi.android.nrfmesh.core.ui.BottomSheetTopAppBar
import no.nordicsemi.android.nrfmesh.core.ui.MeshTwoLineListItem

@Composable
internal fun ProxyRoute() {
    ProxyFilterScreen()
}

@Composable
private fun ProxyFilterScreen() {
    val scope = rememberCoroutineScope()
    var showProxiesSheet by rememberSaveable { mutableStateOf(false) }
    val capabilitiesSheet = rememberModalBottomSheetState()

    LazyColumn {
        proxyFilterInfo(onProxyRowClicked = { showProxiesSheet = true })
    }
    if (showProxiesSheet)
        ModalBottomSheet(
            onDismissRequest = { showProxiesSheet = false },
            sheetState = capabilitiesSheet
        ) {
            BottomSheetTopAppBar(
                navigationIcon = Icons.Rounded.Close,
                onNavigationIconClick = {
                    scope.launch {
                        capabilitiesSheet.hide()
                        delay(1000)
                        showProxiesSheet = false
                    }
                },
                title = "Proxies",
                titleStyle = MaterialTheme.typography.titleLarge
            )
            ScannerSection(onDeviceFound = {})
        }
}

private fun LazyListScope.proxyFilterInfo(onProxyRowClicked: () -> Unit) {
    item { AutomaticConnectionRow() }
    item { ProxyRow(onProxyRowClicked = onProxyRowClicked) }
}

@Composable
private fun AutomaticConnectionRow() {
    var isChecked by rememberSaveable { mutableStateOf(true) }
    MeshTwoLineListItem(
        modifier = Modifier
            .padding(end = 16.dp)
            .clickable(onClick = { }),
        leadingComposable = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        title = "Automatic Connection",
        trailingComposable = {
            Switch(checked = isChecked, onCheckedChange = { isChecked = it })
        }
    )
}

@Composable
private fun ProxyRow(onProxyRowClicked: () -> Unit) {
    MeshTwoLineListItem(
        modifier = Modifier.padding(end = 16.dp).clickable(onClick = onProxyRowClicked),
        leadingComposable = {
            Icon(
                modifier = Modifier.padding(all = 16.dp),
                imageVector = Icons.Outlined.Hub,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        title = "Proxy",
        subtitle = "No device connected",
        trailingComposable = {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Divider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                )
                Spacer(modifier = Modifier.size(16.dp))
                IconButton(
                    onClick = onProxyRowClicked
                ) {
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
private fun ScannerSection(onDeviceFound: (BleScanResults) -> Unit) {
    ScannerView(
        uuid = ParcelUuid(MeshProxyService.uuid),
        onResult = onDeviceFound,
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