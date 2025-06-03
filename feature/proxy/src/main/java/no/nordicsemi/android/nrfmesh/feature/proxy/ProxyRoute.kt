package no.nordicsemi.android.nrfmesh.feature.proxy

import android.content.Context
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Lan
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.permissions.ble.RequireBluetooth
import no.nordicsemi.android.common.permissions.ble.RequireLocation
import no.nordicsemi.kotlin.mesh.bearer.gatt.utils.MeshProxyService
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.core.common.fixedGroupAddresses
import no.nordicsemi.android.nrfmesh.core.common.name
import no.nordicsemi.android.nrfmesh.core.data.NetworkConnectionState
import no.nordicsemi.android.nrfmesh.core.data.ProxyConnectionState
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.MeshIconButton
import no.nordicsemi.android.nrfmesh.core.ui.MeshMessageStatusDialog
import no.nordicsemi.android.nrfmesh.core.ui.MeshOutlinedButton
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.core.ui.SwipeDismissItem
import no.nordicsemi.android.nrfmesh.core.ui.isCompactWidth
import no.nordicsemi.android.nrfmesh.feature.scanner.navigation.ScannerScreenRoute
import no.nordicsemi.kotlin.ble.client.android.ScanResult
import no.nordicsemi.kotlin.mesh.core.ProxyFilterType
import no.nordicsemi.kotlin.mesh.core.messages.proxy.AddAddressesToFilter
import no.nordicsemi.kotlin.mesh.core.messages.proxy.ProxyConfigurationMessage
import no.nordicsemi.kotlin.mesh.core.messages.proxy.RemoveAddressesFromFilter
import no.nordicsemi.kotlin.mesh.core.messages.proxy.SetFilterType
import no.nordicsemi.kotlin.mesh.core.model.FixedGroupAddress
import no.nordicsemi.kotlin.mesh.core.model.GroupAddress
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.ProxyFilterAddress
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.VirtualAddress
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun ProxyRoute(
    uiState: ProxyScreenUiState,
    onBluetoothEnabled: (Boolean) -> Unit,
    onLocationEnabled: (Boolean) -> Unit,
    onAutoConnectToggled: (Boolean) -> Unit,
    onScanResultSelected: (Context, ScanResult) -> Unit,
    onDisconnectClicked: () -> Unit,
    send: (ProxyConfigurationMessage) -> Unit,
    resetMessageState: () -> Unit,
) {
    RequireBluetooth(onChanged = onBluetoothEnabled) {
        RequireLocation(onChanged = onLocationEnabled) {
            Column(
                modifier = Modifier.verticalScroll(state = rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProxyFilterInfo(
                    proxyConnectionState = uiState.proxyConnectionState,
                    onAutoConnectToggled = onAutoConnectToggled,
                    onScanResultSelected = onScanResultSelected,
                    onDisconnectClicked = onDisconnectClicked
                )
                FilterSection(
                    network = uiState.network,
                    type = uiState.filterType,
                    addresses = uiState.addresses,
                    isConnected = uiState.proxyConnectionState.connectionState is NetworkConnectionState.Connected,
                    limitReached = uiState.isProxyLimitReached,
                    send = send,
                    messageState = uiState.messageState,
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
    onScanResultSelected: (Context, ScanResult) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
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
                onScanResultSelected = { result ->
                    showProxyScannerSheet = false
                    onScanResultSelected(context, result)
                    scope.launch {
                        proxyScannerSheetState.hide()
                    }.invokeOnCompletion {
                        if (!proxyScannerSheetState.isVisible) {
                            showProxyScannerSheet = false
                        }
                    }
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

@OptIn(ExperimentalUuidApi::class)
@Composable
private fun ScannerSection(onScanResultSelected: (ScanResult) -> Unit) {
    ScannerScreenRoute(
        uuid = MeshProxyService.uuid,
        onScanResultSelected = onScanResultSelected
    )
}

@Composable
private fun NetworkConnectionState.describe() = when (this) {
    NetworkConnectionState.Scanning -> stringResource(R.string.label_scanning)
    is NetworkConnectionState.Connecting -> stringResource(
        R.string.label_connecting_to, peripheral.name ?: R.string.label_unknown
    )

    is NetworkConnectionState.Connected -> stringResource(
        R.string.label_connected_to, peripheral.name ?: R.string.label_unknown
    )

    NetworkConnectionState.Disconnected -> stringResource(R.string.label_proxy_disconnected)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSection(
    network: MeshNetwork?,
    type: ProxyFilterType?,
    addresses: List<ProxyFilterAddress> = emptyList(),
    limitReached: Boolean,
    isConnected: Boolean,
    send: (ProxyConfigurationMessage) -> Unit,
    messageState: MessageState,
    resetMessageState: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val options =
        listOf<ProxyFilterType>(ProxyFilterType.INCLUSION_LIST, ProxyFilterType.EXCLUSION_LIST)
    var selectedIndex by remember {
        mutableIntStateOf(if (type == null) 0 else options.indexOf(type))
    }
    var showBottomSheet by remember { mutableStateOf(false) }
    if (isCompactWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp)
                .padding(vertical = 8.dp),
        ) {
            SectionTitle(title = stringResource(R.string.label_filter_type))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (isConnected) {
                    MeshIconButton(
                        isOnClickActionInProgress = messageState.isInProgress()
                                && messageState.message is AddAddressesToFilter,
                        buttonIcon = Icons.Outlined.Add,
                        onClick = { showBottomSheet = true },
                        enabled = !messageState.isInProgress()
                    )
                }
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
                            enabled = isConnected
                        )
                    }
                }
            }
        }
    } else {
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
            MeshOutlinedButton(
                isOnClickActionInProgress = messageState.isInProgress()
                        && messageState.message is AddAddressesToFilter,
                buttonIcon = Icons.Outlined.Add,
                text = stringResource(R.string.label_add_address),
                onClick = { showBottomSheet = true },
                enabled = isConnected && !messageState.isInProgress()
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
                        enabled = isConnected
                    )
                }
            }
        }
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        addresses.forEach {
            SwipeToDismissAddress(
                network = network,
                address = it,
                onSwiped = {
                    send(RemoveAddressesFromFilter(addresses = listOf(it)))
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
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            content = {
                network?.let { network ->
                    Addresses(
                        network = network,
                        onAddressClicked = {
                            send(AddAddressesToFilter(addresses = listOf(it)))
                            scope.launch {
                                sheetState.hide()
                            }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    showBottomSheet = false
                                }
                            }
                        }
                    )
                }
            }
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
        content = { AddressRow(address = address, network = network) }
    )
}

@Composable
private fun Addresses(
    network: MeshNetwork,
    onAddressClicked: ((ProxyFilterAddress) -> Unit),
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(R.string.label_elements)
        )
        network.nodes.flatMap { it.elements }.forEach { element ->
            AddressRow(
                network = network,
                address = element.unicastAddress,
                onClick = { onAddressClicked(element.unicastAddress as ProxyFilterAddress) }
            )
        }
        if (network.groups.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = stringResource(R.string.label_groups)
            )
        }
        network.groups.forEach { group ->
            AddressRow(
                address = group.address as ProxyFilterAddress,
                network = network,
                onClick = { onAddressClicked(group.address as ProxyFilterAddress) }
            )
        }
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(R.string.label_fixed_group_addresses)
        )
        fixedGroupAddresses.forEach { destination ->
            AddressRow(
                address = destination as ProxyFilterAddress,
                network = network,
                onClick = { onAddressClicked(destination) }
            )
        }
    }
}

@Composable
private fun AddressRow(
    address: ProxyFilterAddress,
    network: MeshNetwork?,
    onClick: (() -> Unit)? = null,
) {
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
        } else null,
        onClick = onClick
    )
}
