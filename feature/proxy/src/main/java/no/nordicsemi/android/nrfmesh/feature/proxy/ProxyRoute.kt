package no.nordicsemi.android.nrfmesh.feature.proxy

import android.content.Context
import android.os.ParcelUuid
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Lan
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.permissions.ble.RequireBluetooth
import no.nordicsemi.android.common.permissions.ble.RequireLocation
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResults
import no.nordicsemi.android.kotlin.ble.ui.scanner.ScannerView
import no.nordicsemi.android.kotlin.ble.ui.scanner.WithServiceUuid
import no.nordicsemi.android.kotlin.ble.ui.scanner.main.DeviceListItem
import no.nordicsemi.android.kotlin.mesh.bearer.android.utils.MeshProxyService
import no.nordicsemi.android.nrfmesh.core.common.name
import no.nordicsemi.android.nrfmesh.core.data.NetworkConnectionState
import no.nordicsemi.android.nrfmesh.core.data.ProxyConnectionState
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.MeshMessageStatusDialog
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.core.ui.SwipeDismissItem
import no.nordicsemi.kotlin.mesh.core.ProxyFilterType
import no.nordicsemi.kotlin.mesh.core.messages.proxy.ProxyConfigurationMessage
import no.nordicsemi.kotlin.mesh.core.messages.proxy.RemoveAddressesFromFilter
import no.nordicsemi.kotlin.mesh.core.messages.proxy.SetFilterType
import no.nordicsemi.kotlin.mesh.core.model.FixedGroupAddress
import no.nordicsemi.kotlin.mesh.core.model.GroupAddress
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.ProxyFilterAddress
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.VirtualAddress

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun ProxyRoute(
    uiState: ProxyScreenUiState,
    onBluetoothEnabled: (Boolean) -> Unit,
    onLocationEnabled: (Boolean) -> Unit,
    onAutoConnectToggled: (Boolean) -> Unit,
    onDeviceFound: (Context, BleScanResults) -> Unit,
    onDisconnectClicked: () -> Unit,
    send: (ProxyConfigurationMessage) -> Unit,
    resetMessageState: () -> Unit,
) {
    RequireBluetooth(onChanged = onBluetoothEnabled) {
        RequireLocation(onChanged = onLocationEnabled) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProxyFilterInfo(
                    proxyConnectionState = uiState.proxyConnectionState,
                    onAutoConnectToggled = onAutoConnectToggled,
                    onDeviceFound = onDeviceFound,
                    onDisconnectClicked = onDisconnectClicked
                )
                FilterSection(
                    network = uiState.network,
                    type = uiState.filterType,
                    addresses = uiState.addresses,
                    isEnabled = uiState.proxyConnectionState.connectionState is NetworkConnectionState.Connected,
                    limitReached = uiState.isProxyLimitReached,
                    send = send,
                    resetMessageState = resetMessageState
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProxyFilterInfo(
    proxyConnectionState: ProxyConnectionState,
    onAutoConnectToggled: (Boolean) -> Unit,
    onDisconnectClicked: () -> Unit,
    onDeviceFound: (Context, BleScanResults) -> Unit,
) {
    var showProxyScannerSheet by rememberSaveable { mutableStateOf(false) }
    val proxyScannerSheetState = rememberModalBottomSheetState()
    AutomaticConnectionRow(
        proxyConnectionState = proxyConnectionState,
        onAutoConnectToggled = onAutoConnectToggled,
        onConnectClicked = { showProxyScannerSheet = true },
        onDisconnectClicked = onDisconnectClicked
    )
    if (showProxyScannerSheet)
        ModalBottomSheet(
            onDismissRequest = { showProxyScannerSheet = false },
            sheetState = proxyScannerSheetState
        ) {
            SectionTitle(title = stringResource(R.string.label_proxies))
            ScannerSection(
                onDeviceFound = { context, results ->
                    showProxyScannerSheet = false
                    onDeviceFound(context, results)
                }
            )
        }
}

@Composable
private fun AutomaticConnectionRow(
    proxyConnectionState: ProxyConnectionState,
    onAutoConnectToggled: (Boolean) -> Unit,
    onConnectClicked: () -> Unit,
    onDisconnectClicked: () -> Unit,
) {
    ElevatedCardItem(
        modifier = Modifier
            .padding(top = 8.dp)
            .padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.AutoAwesome,
        title = stringResource(R.string.label_automatic_connection),
        titleAction = {
            Switch(
                modifier = Modifier.padding(start = 16.dp),
                checked = proxyConnectionState.autoConnect,
                onCheckedChange = { onAutoConnectToggled(it) }
            )
        },
        subtitle = proxyConnectionState.connectionState.describe(),
        supportingText = stringResource(R.string.label_automatic_connection_rationale),
        actions = {
            OutlinedButton(
                onClick = onConnectClicked,
                enabled = !proxyConnectionState.autoConnect,
                content = { Text(text = stringResource(R.string.label_connect)) }
            )
            Spacer(modifier = Modifier.size(16.dp))
            OutlinedButton(
                onClick = {
                    onAutoConnectToggled(false)
                    onDisconnectClicked()
                },
                content = { Text(text = stringResource(R.string.label_disconnect)) }
            )
        }
    )
}

@Composable
private fun ScannerSection(
    onDeviceFound: (Context, BleScanResults) -> Unit,
) {
    val context = LocalContext.current
    val filters = listOf(
        WithServiceUuid(title = "Provisioned", uuid = ParcelUuid(MeshProxyService.uuid))
    )
    ScannerView(
        filters = filters,
        onResult = { onDeviceFound(context, it) },
        filterShape = MaterialTheme.shapes.small,
        deviceItem = {
            DeviceListItem(
                modifier = Modifier.padding(vertical = 16.dp),
                name = it.device.name,
                address = it.device.address
            )
        },
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

@Composable
private fun FilterSection(
    network: MeshNetwork?,
    type: ProxyFilterType?,
    addresses: List<ProxyFilterAddress> = emptyList(),
    limitReached: Boolean,
    isEnabled: Boolean,
    send: (ProxyConfigurationMessage) -> Unit,
    resetMessageState: () -> Unit,
) {
    val options =
        listOf<ProxyFilterType>(ProxyFilterType.INCLUSION_LIST, ProxyFilterType.EXCLUSION_LIST)
    var selectedIndex by remember {
        mutableIntStateOf(if (type == null) 0 else options.indexOf(type))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 16.dp)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SectionTitle(
            modifier = Modifier.weight(weight = 1f),
            title = stringResource(R.string.label_filter_type)
        )
        SingleChoiceSegmentedButtonRow {
            options.forEachIndexed { index, label ->
                SegmentedButton(
                    modifier = Modifier.defaultMinSize(minWidth = 150.dp),
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = options.size
                    ),
                    onClick = {
                        selectedIndex = index
                        send(SetFilterType(options[selectedIndex]))
                    },
                    selected = index == selectedIndex,
                    label = { Text(text = label.toString()) },
                    enabled = isEnabled
                )
            }
        }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = addresses, key = { it.address.toInt() }) { address ->
            SwipeToDismissAddress(
                network = network,
                address = address,
                onSwiped = {
                    send(RemoveAddressesFromFilter(listOf(address)))
                }
            )
        }
    }

    if (limitReached) {
        MeshMessageStatusDialog(
            text = stringResource(R.string.label_proxy_filter_limit_reached),
            showDismissButton = true,
            onDismissRequest = resetMessageState,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissAddress(
    network: MeshNetwork?,
    address: ProxyFilterAddress,
    onSwiped: (ProxyFilterAddress) -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            onSwiped(address)
            true
        }
    )
    SwipeDismissItem(
        dismissState = dismissState,
        content = {
            println(
                "address ${
                    when (address) {
                        is UnicastAddress -> "Unicast"
                        is VirtualAddress -> "Virtual"
                        is GroupAddress -> "Group"
                        is FixedGroupAddress -> "Group"
                    }
                }"
            )
            ElevatedCardItem(
                imageVector = Icons.Outlined.Lan,
                title = when (address) {
                    is UnicastAddress -> network?.element(address.address)?.name
                        ?: stringResource(R.string.label_unknown)

                    is VirtualAddress -> network?.group(address.address)?.name
                        ?: stringResource(R.string.label_unknown)

                    is GroupAddress -> network?.group(address.address)?.name
                        ?: stringResource(R.string.label_unknown)

                    is FixedGroupAddress -> address.name()
                },
                subtitle = if (address is UnicastAddress) {
                    "${network?.node(address.address)?.name ?: "Unknown"} : 0x${address.toHexString()}"
                } else null
            )
        }
    )
}