package no.nordicsemi.android.nrfmesh.feature.nodes.node

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.DeviceHub
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Recycling
import androidx.compose.material.icons.outlined.SafetyCheck
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItemTextField
import no.nordicsemi.android.nrfmesh.core.ui.MeshAlertDialog
import no.nordicsemi.android.nrfmesh.core.ui.MeshOutlinedButton
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.core.ui.SwitchWithIcon
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
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NodeListPane(
    messageState: MessageState,
    nodeData: NodeInfoListData,
    node: Node,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    highlightSelectedItem: Boolean,
    selectedItem: ClickableNodeInfoItem?,
    onNetworkKeysClicked: (UUID) -> Unit,
    onApplicationKeysClicked: (UUID) -> Unit,
    onElementClicked: (Address) -> Unit,
    onExcluded: (Boolean) -> Unit,
    send: (AcknowledgedConfigMessage) -> Unit,
    save: () -> Unit,
    navigateBack: () -> Unit,
) {
    val state = rememberPullToRefreshState()
    val scrollState = rememberScrollState()
    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        state = state,
        onRefresh = onRefresh,
        isRefreshing = isRefreshing
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = scrollState),
            verticalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            SectionTitle(
                modifier = Modifier.padding(top = 8.dp),
                title = stringResource(R.string.label_node)
            )
            NodeNameRow(
                name = nodeData.name,
                onNameChanged = {
                    node.name = it
                    save()
                }
            )
            SectionTitle(title = stringResource(id = R.string.title_keys))
            NetworkKeysRow(
                count = nodeData.networkKeyCount,
                isSelected = selectedItem == ClickableNodeInfoItem.NetworkKeys
                        && highlightSelectedItem,
                onNetworkKeysClicked = { onNetworkKeysClicked(nodeData.uuid) }
            )
            ApplicationKeysRow(
                count = nodeData.appKeyCount,
                isSelected = selectedItem == ClickableNodeInfoItem.ApplicationKeys
                        && highlightSelectedItem,
                onApplicationKeysClicked = { onApplicationKeysClicked(nodeData.uuid) }
            )
            SectionTitle(title = stringResource(id = R.string.title_elements))
            nodeData.elements.forEachIndexed { index, element ->
                ElementRow(
                    element = element,
                    isSelected = (selectedItem as? ClickableNodeInfoItem.Element)?.address
                            == element.unicastAddress.address
                            && highlightSelectedItem,
                    onElementsClicked = { onElementClicked(element.unicastAddress.address) }
                )
                if (index != nodeData.elements.size - 1) Spacer(modifier = Modifier.size(8.dp))
            }
            SectionTitle(title = stringResource(id = R.string.title_node_information))
            NodeInformationRow(nodeData)
            SectionTitle(title = stringResource(id = R.string.title_time_to_live))
            DefaultTtlRow(ttl = nodeData.defaultTtl, messageState = messageState, send = send)
            SectionTitle(title = stringResource(id = R.string.title_proxy_state))
            ProxyStateRow(
                messageState = messageState,
                proxy = nodeData.features.proxy,
                send = send
            )
            SectionTitle(title = stringResource(id = R.string.title_exclusions))
            ExclusionRow(isExcluded = nodeData.excluded, onExcluded = onExcluded)
            SectionTitle(title = stringResource(id = R.string.label_reset_node))
            ResetRow(
                messageState = messageState,
                navigateBack = navigateBack,
                send = send,
            )
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

@Composable
private fun NodeInformationRow(node: NodeInfoListData) {
    CompanyIdentifier(companyIdentifier = node.companyIdentifier)
    ProductIdentifier(productIdentifier = node.productIdentifier)
    ProductVersion(productVersion = node.versionIdentifier)
    ReplayProtectionCount(replayProtectionCount = node.replayProtectionCount)
    Security(node = node)
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
private fun CompanyIdentifier(companyIdentifier: UShort?) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Work,
        title = stringResource(R.string.label_company_identifier),
        subtitle = companyIdentifier?.toHexString()?.uppercase()
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
        subtitle = productIdentifier?.toHexString()?.uppercase()
            ?: stringResource(R.string.unknown),
    )
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
private fun ProductVersion(productVersion: UShort?) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Numbers,
        title = stringResource(R.string.label_product_version),
        subtitle = productVersion?.toHexString()?.uppercase() ?: stringResource(R.string.unknown),
    )
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
private fun ReplayProtectionCount(replayProtectionCount: UShort?) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.SafetyCheck,
        title = stringResource(R.string.label_replay_protection_count),
        subtitle = replayProtectionCount?.toHexString()?.uppercase()
            ?: stringResource(R.string.unknown),
    )
}

@Composable
private fun Security(node: NodeInfoListData) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Security,
        title = stringResource(R.string.label_security),
        subtitle = when (node.elements.isEmpty()) {
            true -> node.security.toString()
            false -> stringResource(id = R.string.unknown)
        }
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
    var enabled by rememberSaveable {
        mutableStateOf(proxy?.state?.let { it == FeatureState.Enabled } ?: false)
    }
    var showProxyStateDialog by rememberSaveable { mutableStateOf(false) }
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Hub,
        title = stringResource(R.string.label_gatt_proxy_state),
        titleAction = {
            SwitchWithIcon(isChecked = proxy?.state == FeatureState.Enabled, onCheckedChange = {
                // enabled = it
                if (!it) showProxyStateDialog = true
                else send(ConfigGattProxySet(FeatureState.Enabled))
            })
        },
        subtitle = "Proxy state is ${if (enabled) "enabled" else "disabled"}",
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
                showProxyStateDialog = !showProxyStateDialog
                enabled = proxy?.state?.let { it == FeatureState.Enabled } ?: false
            },
            icon = Icons.Outlined.Hub,
            title = stringResource(R.string.label_disable_proxy_feature),
            text = stringResource(R.string.label_are_you_sure_rationale),
            iconColor = Color.Red,
            onConfirmClick = {
                enabled = false
                send(ConfigGattProxySet(state = FeatureState.Disabled))
                showProxyStateDialog = !showProxyStateDialog
            },
            onDismissClick = {
                showProxyStateDialog = !showProxyStateDialog
                enabled = proxy?.state?.let { it == FeatureState.Enabled } ?: false
            }
        )
    }
}

@Composable
private fun ExclusionRow(isExcluded: Boolean, onExcluded: (Boolean) -> Unit) {
    var excluded by rememberSaveable { mutableStateOf(isExcluded) }
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Block,
        title = stringResource(R.string.label_exclude_node),
        titleAction = {
            SwitchWithIcon(
                isChecked = isExcluded,
                onCheckedChange = {
                    excluded = it
                    onExcluded(it)
                }
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