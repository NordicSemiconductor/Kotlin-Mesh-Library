package no.nordicsemi.android.nrfmesh.feature.nodes.node

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.DeviceHub
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Lan
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Recycling
import androidx.compose.material.icons.outlined.SafetyCheck
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.core.common.copyToClipboard
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItemTextField
import no.nordicsemi.android.nrfmesh.core.ui.MeshAlertDialog
import no.nordicsemi.android.nrfmesh.core.ui.MeshOutlinedButton
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.feature.nodes.R
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigGattProxyGet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigGattProxySet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNodeReset
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNodeResetStatus
import no.nordicsemi.kotlin.mesh.core.model.Address
import no.nordicsemi.kotlin.mesh.core.model.FeatureState
import no.nordicsemi.kotlin.mesh.core.model.Node
import no.nordicsemi.kotlin.mesh.core.model.Proxy
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.util.CompanyIdentifier
import java.util.Locale.ROOT
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
internal fun NodeListScreen(
    messageState: MessageState,
    nodeData: NodeInfoListData,
    node: Node,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    highlightSelectedItem: Boolean,
    selectedItem: ClickableNodeInfoItem?,
    onNetworkKeysClicked: (Uuid) -> Unit,
    onApplicationKeysClicked: (Uuid) -> Unit,
    onElementClicked: (Address) -> Unit,
    onExcluded: (Boolean) -> Unit,
    send: (AcknowledgedConfigMessage) -> Unit,
    save: () -> Unit,
    navigateBack: () -> Unit,
    removeNode: () -> Unit,
) {
    val state = rememberPullToRefreshState()
    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        state = state,
        onRefresh = onRefresh,
        isRefreshing = isRefreshing
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(space = 8.dp),
        ) {
            item {
                SectionTitle(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .padding(horizontal = 16.dp),
                    title = stringResource(R.string.label_node)
                )
            }
            item {
                NodeNameRow(
                    name = nodeData.name,
                    onNameChanged = {
                        node.name = it
                        save()
                    }
                )
            }
            item { AddressRow(address = nodeData.address) }
            item {
                SectionTitle(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    title = stringResource(id = R.string.title_keys)
                )
            }
            item {
                DeviceKeyRow(
                    deviceKey = nodeData.deviceKey ?: stringResource(R.string.unknown)
                )
            }
            item {
                NetworkKeysRow(
                    count = nodeData.netKeys.size,
                    isSelected = selectedItem == ClickableNodeInfoItem.NetworkKeys
                            && highlightSelectedItem,
                    onNetworkKeysClicked = { onNetworkKeysClicked(nodeData.uuid) }
                )
            }
            item {
                ApplicationKeysRow(
                    count = nodeData.appKeys.size,
                    isSelected = selectedItem == ClickableNodeInfoItem.ApplicationKeys
                            && highlightSelectedItem,
                    onApplicationKeysClicked = { onApplicationKeysClicked(nodeData.uuid) }
                )
            }
            item {
                SectionTitle(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    title = stringResource(id = R.string.title_elements)
                )
            }
            items(items = nodeData.elements, key = { it.index }) { element ->
                ElementRow(
                    element = element,
                    isSelected = (selectedItem as? ClickableNodeInfoItem.Element)?.address
                            == element.unicastAddress.address
                            && highlightSelectedItem,
                    onElementsClicked = { onElementClicked(element.unicastAddress.address) }
                )
            }
            item {
                SectionTitle(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    title = stringResource(id = R.string.title_node_information)
                )
            }
            item { CompanyIdentifier(companyIdentifier = nodeData.companyIdentifier) }
            item { ProductIdentifier(productIdentifier = nodeData.productIdentifier) }
            item { ProductVersion(productVersion = nodeData.versionIdentifier) }
            item { ReplayProtectionCount(replayProtectionCount = nodeData.replayProtectionCount) }
            item { Security(node = nodeData) }
            item {
                SectionTitle(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    title = stringResource(id = R.string.title_time_to_live)
                )
            }
            item {
                DefaultTtlRow(
                    ttl = nodeData.defaultTtl,
                    messageState = messageState,
                    send = send
                )
            }
            item {
                SectionTitle(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    title = stringResource(id = R.string.title_proxy_state)
                )
            }
            item {
                ProxyStateRow(
                    messageState = messageState,
                    proxy = nodeData.features.proxy,
                    send = send
                )
            }
            item {
                SectionTitle(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    title = stringResource(id = R.string.title_exclusions)
                )
            }
            item { ExclusionRow(isExcluded = nodeData.excluded, onExcluded = onExcluded) }
            item {
                SectionTitle(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    title = stringResource(id = R.string.label_reset_node)
                )
            }
            item { ResetRow(messageState = messageState, navigateBack = navigateBack, send = send) }
            item {
                SectionTitle(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    title = stringResource(id = R.string.label_remove_node)
                )
            }
            item {
                RemoveNode(
                    navigateBack = navigateBack,
                    removeNode = removeNode
                )
            }
        }
    }
}

@Composable
private fun NodeNameRow(name: String, onNameChanged: (String) -> Unit) {
    ElevatedCardItemTextField(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Badge,
        title = stringResource(id = R.string.label_name),
        subtitle = name,
        placeholder = stringResource(id = R.string.label_placeholder_node_name),
        onValueChanged = onNameChanged
    )
}

@Composable
private fun AddressRow(address: UnicastAddress) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Lan,
        title = stringResource(id = R.string.label_unicast_address),
        subtitle = address.address.toHexString(
            format = HexFormat {
                number.prefix = "0x"
                upperCase = true
            }
        )
    )
}

@Composable
private fun DeviceKeyRow(deviceKey: String) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.VpnKey,
        title = stringResource(id = R.string.label_device_key),
        subtitle = deviceKey,
        onClick = {
            copyToClipboard(
                scope = scope,
                clipboard = clipboard,
                text = deviceKey,
                label = context.getString(R.string.label_device_key)
            )
        }
    )
}

@Composable
private fun NetworkKeysRow(count: Int, isSelected: Boolean, onNetworkKeysClicked: () -> Unit) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        colors = when (isSelected) {
            true -> CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )

            else -> CardDefaults.outlinedCardColors()
        },
        imageVector = Icons.Outlined.VpnKey,
        onClick = onNetworkKeysClicked,
        title = stringResource(R.string.label_network_keys),
        subtitle = "$count ${if (count == 1) "key" else "keys"} added"
    )
}

@Composable
private fun ApplicationKeysRow(
    count: Int,
    isSelected: Boolean,
    onApplicationKeysClicked: () -> Unit,
) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        colors = when (isSelected) {
            true -> CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )

            else -> CardDefaults.outlinedCardColors()
        },
        onClick = onApplicationKeysClicked,
        imageVector = Icons.Outlined.VpnKey,
        title = stringResource(R.string.label_application_keys),
        subtitle = "$count ${if (count == 1) "key" else "keys"} added"
    )
}

@Composable
private fun ElementRow(
    element: ElementListData,
    isSelected: Boolean,
    onElementsClicked: () -> Unit,
) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        colors = when (isSelected) {
            true -> CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )

            else -> CardDefaults.outlinedCardColors()
        },
        onClick = onElementsClicked,
        imageVector = Icons.Outlined.DeviceHub,
        title = element.name ?: "Unknown",
        subtitle = "${element.models.size} ${if (element.models.size == 1) "model" else "models"}"
    )
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
private fun CompanyIdentifier(companyIdentifier: UShort?) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.WorkOutline,
        title = stringResource(R.string.label_company_identifier),
        subtitle = companyIdentifier
            ?.let {
                CompanyIdentifier.name(id = it) ?: it
                    .toHexString(
                        format = HexFormat {
                            number.prefix = "0x"
                            upperCase = true
                        }
                    ).uppercase()
            }
            ?: stringResource(R.string.unknown),
    )
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
private fun ProductIdentifier(productIdentifier: UShort?) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.QrCode,
        title = stringResource(R.string.label_product_identifier),
        subtitle = productIdentifier?.toHexString(
            format = HexFormat {
                number.prefix = "0x"
                upperCase = true
            }
        ) ?: stringResource(R.string.unknown),
    )
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
private fun ProductVersion(productVersion: UShort?) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Numbers,
        title = stringResource(R.string.label_product_version),
        subtitle = productVersion?.toHexString(
            format = HexFormat {
                number.prefix = "0x"
                upperCase = true
            }
        ) ?: stringResource(R.string.unknown),
    )
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
private fun ReplayProtectionCount(replayProtectionCount: UShort?) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.SafetyCheck,
        title = stringResource(R.string.label_replay_protection_count),
        subtitle = "${replayProtectionCount ?: stringResource(R.string.unknown)}",
    )
}

@Composable
private fun Security(node: NodeInfoListData) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Security,
        title = stringResource(R.string.label_security),
        subtitle = node.security.toString().replaceFirstChar { it.titlecase(locale = ROOT) }
    )
}

@Composable
private fun DefaultTtlRow(
    ttl: UByte?,
    messageState: MessageState,
    send: (AcknowledgedConfigMessage) -> Unit,
) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Timer,
        title = stringResource(R.string.label_default_time_to_live),
        subtitle = if (ttl != null) "TTL set to $ttl" else "Unknown",
        supportingText = stringResource(R.string.label_default_ttl_rationale)
    ) {
        MeshOutlinedButton(
            onClick = { send(ConfigGattProxyGet()) },
            text = stringResource(R.string.label_get_ttl),
            buttonIcon = Icons.Outlined.Download,
            enabled = !messageState.isInProgress(),
            isOnClickActionInProgress = messageState.isInProgress()
                    && messageState.message is ConfigGattProxyGet
        )
        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
        MeshOutlinedButton(
            onClick = { send(ConfigGattProxySet(FeatureState.Enabled)) },
            text = stringResource(R.string.label_set_ttl),
            buttonIcon = Icons.Outlined.Upload,
            enabled = !messageState.isInProgress(),
            isOnClickActionInProgress = messageState.isInProgress()
                    && messageState.message is ConfigGattProxySet
        )
    }
}

@Composable
private fun ProxyStateRow(
    proxy: Proxy?,
    messageState: MessageState,
    send: (AcknowledgedConfigMessage) -> Unit,
) {
    var showProxyStateDialog by rememberSaveable { mutableStateOf(false) }
    val isEnabled = proxy?.state == FeatureState.Enabled
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Hub,
        title = stringResource(R.string.label_gatt_proxy_state),
        titleAction = {
            Switch(
                modifier = Modifier.padding(horizontal = 16.dp),
                checked = isEnabled,
                onCheckedChange = {
                    if (!it) showProxyStateDialog = true
                    else send(ConfigGattProxySet(FeatureState.Enabled))
                }
            )
        },
        subtitle = "Proxy state is ${if (isEnabled) "enabled" else "disabled"}",
        supportingText = stringResource(R.string.label_proxy_state_rationale)
    ) {
        MeshOutlinedButton(
            onClick = { send(ConfigGattProxyGet()) },
            text = stringResource(R.string.label_get_state),
            buttonIcon = Icons.Outlined.Download,
            enabled = !messageState.isInProgress(),
            isOnClickActionInProgress = messageState.isInProgress()
                    && messageState.message is ConfigGattProxyGet
        )
    }
    if (showProxyStateDialog) {
        MeshAlertDialog(
            onDismissRequest = {
                showProxyStateDialog = false
            },
            icon = Icons.Outlined.Hub,
            title = stringResource(R.string.label_disable_proxy_feature),
            text = stringResource(R.string.label_are_you_sure_rationale),
            iconColor = Color.Red,
            onConfirmClick = {
                send(ConfigGattProxySet(state = FeatureState.Disabled))
                showProxyStateDialog = false
            },
            onDismissClick = {
                showProxyStateDialog = false
            }
        )
    }
}

@Composable
private fun ExclusionRow(isExcluded: Boolean, onExcluded: (Boolean) -> Unit) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Block,
        title = stringResource(R.string.label_exclude_node),
        titleAction = {
            Switch(
                modifier = Modifier.padding(horizontal = 16.dp),
                checked = isExcluded,
                onCheckedChange = onExcluded
            )
        },
        supportingText = stringResource(R.string.label_exclusion_rationale),
        body = { Spacer(modifier = Modifier.size(8.dp)) }
    )
}

@Composable
private fun ResetRow(
    messageState: MessageState,
    send: (AcknowledgedConfigMessage) -> Unit,
    navigateBack: () -> Unit,
) {
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Recycling,
        title = stringResource(R.string.label_reset_node),
        supportingText = stringResource(R.string.label_reset_node_rationale)
    ) {
        MeshOutlinedButton(
            border = BorderStroke(width = 1.dp, color = Color.Red),
            onClick = { showResetDialog = !showResetDialog },
            text = stringResource(R.string.label_reset),
            buttonIcon = Icons.Outlined.Recycling,
            buttonIconTint = Color.Red,
            textColor = Color.Red,
            enabled = !messageState.isInProgress(),
            isOnClickActionInProgress = messageState.isInProgress()
                    && messageState.message is ConfigNodeReset
        )
    }
    if (showResetDialog) {
        MeshAlertDialog(
            onDismissRequest = { showResetDialog = !showResetDialog },
            icon = Icons.Outlined.Recycling,
            title = stringResource(R.string.label_reset_node),
            text = stringResource(R.string.label_are_you_sure_rationale),
            iconColor = Color.Red,
            onDismissClick = { showResetDialog = !showResetDialog },
            onConfirmClick = {
                showResetDialog = !showResetDialog
                send(ConfigNodeReset())
            }
        )
    }
    if (messageState.didSucceed() && messageState.response is ConfigNodeResetStatus) {
        navigateBack()
    }
}

@Composable
private fun RemoveNode(
    removeNode: () -> Unit,
    navigateBack: () -> Unit,
) {
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.DeleteForever,
        title = stringResource(R.string.label_remove_node),
        supportingText = stringResource(R.string.label_remove_node_rationale)
    ) {
        MeshOutlinedButton(
            border = BorderStroke(width = 1.dp, color = Color.Red),
            onClick = { showResetDialog = !showResetDialog },
            text = stringResource(R.string.label_remove),
            buttonIcon = Icons.Outlined.DeleteForever,
            buttonIconTint = Color.Red,
            textColor = Color.Red
        )
    }
    if (showResetDialog) {
        MeshAlertDialog(
            onDismissRequest = { showResetDialog = !showResetDialog },
            icon = Icons.Outlined.DeleteForever,
            title = stringResource(R.string.label_remove_node),
            text = stringResource(R.string.label_are_you_sure_rationale),
            iconColor = Color.Red,
            onDismissClick = { showResetDialog = !showResetDialog },
            onConfirmClick = {
                showResetDialog = !showResetDialog
                removeNode()
                navigateBack()
            }
        )
    }
}