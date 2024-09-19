package no.nordicsemi.android.nrfmesh.feature.nodes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.DeviceHub
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Factory
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Recycling
import androidx.compose.material.icons.outlined.SafetyCheck
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.nrfmesh.core.common.Completed
import no.nordicsemi.android.nrfmesh.core.common.Failed
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.core.common.NotStarted.didFail
import no.nordicsemi.android.nrfmesh.core.common.NotStarted.isInProgress
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItemTextField
import no.nordicsemi.android.nrfmesh.core.ui.MeshAlertDialog
import no.nordicsemi.android.nrfmesh.core.ui.MeshLoadingItems
import no.nordicsemi.android.nrfmesh.core.ui.MeshMessageStatusDialog
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.core.ui.SwitchWithIcon
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.NodeScreen
import no.nordicsemi.kotlin.mesh.core.messages.StatusMessage
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNodeResetStatus
import no.nordicsemi.kotlin.mesh.core.model.Address
import no.nordicsemi.kotlin.mesh.core.model.Element
import no.nordicsemi.kotlin.mesh.core.model.FeatureState
import no.nordicsemi.kotlin.mesh.core.model.Node
import no.nordicsemi.kotlin.mesh.core.model.Proxy
import java.util.UUID

@Composable
fun NodeRoute(
    appState: AppState,
    uiState: NodeScreenUiState,
    onRefresh: () -> Unit,
    onNameChanged: (String) -> Unit,
    onNetworkKeysClicked: (UUID) -> Unit,
    onApplicationKeysClicked: (UUID) -> Unit,
    onElementsClicked: (Address) -> Unit,
    onGetTtlClicked: () -> Unit,
    onProxyStateToggled: (Boolean) -> Unit,
    onGetProxyStateClicked: () -> Unit,
    onExcluded: (Boolean) -> Unit,
    onResetClicked: () -> Unit,
    resetMessageState: () -> Unit,
    onBackPressed: () -> Unit
) {
    val screen = appState.currentScreen as? NodeScreen
    LaunchedEffect(key1 = screen) {
        screen?.buttons?.onEach { button ->
            when (button) {
                NodeScreen.Actions.BACK -> onBackPressed()
            }
        }?.launchIn(this)
    }
    NodeScreen(
        nodeState = uiState.nodeState,
        messageState = uiState.messageState,
        isRefreshing = uiState.isRefreshing,
        onRefresh = onRefresh,
        onNameChanged = onNameChanged,
        onNetworkKeysClicked = onNetworkKeysClicked,
        onApplicationKeysClicked = onApplicationKeysClicked,
        onElementsClicked = onElementsClicked,
        onGetTtlClicked = onGetTtlClicked,
        onProxyStateToggled = onProxyStateToggled,
        onGetProxyStateClicked = onGetProxyStateClicked,
        onExcluded = onExcluded,
        onResetClicked = onResetClicked,
        resetMessageState = resetMessageState,
        onBackPressed = onBackPressed
    )
}

@Composable
private fun NodeScreen(
    nodeState: NodeState,
    messageState: MessageState,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onNameChanged: (String) -> Unit,
    onNetworkKeysClicked: (UUID) -> Unit,
    onApplicationKeysClicked: (UUID) -> Unit,
    onElementsClicked: (Address) -> Unit,
    onGetTtlClicked: () -> Unit,
    onProxyStateToggled: (Boolean) -> Unit,
    onGetProxyStateClicked: () -> Unit,
    onExcluded: (Boolean) -> Unit,
    onResetClicked: () -> Unit,
    resetMessageState: () -> Unit,
    onBackPressed: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(visible = messageState.isInProgress()) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        when (nodeState) {
            NodeState.Loading -> MeshLoadingItems(
                imageVector = Icons.Outlined.VpnKey,
                title = stringResource(id = R.string.title_loading_node)
            )

            is NodeState.Success -> NodeInfo(
                node = nodeState.node,
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                onNameChanged = onNameChanged,
                onNetworkKeysClicked = onNetworkKeysClicked,
                onApplicationKeysClicked = onApplicationKeysClicked,
                onElementsClicked = onElementsClicked,
                onGetTtlClicked = onGetTtlClicked,
                onProxyStateToggled = onProxyStateToggled,
                onGetProxyStateClicked = onGetProxyStateClicked,
                onExcluded = onExcluded,
                onResetClicked = onResetClicked
            )

            is NodeState.Error -> MeshNoItemsAvailable(
                imageVector = Icons.Outlined.ErrorOutline,
                title = nodeState.throwable.message ?: "Unknown error"
            )
        }
        when (messageState) {
            is Failed -> {
                MeshMessageStatusDialog(
                    text = messageState.error.message
                        ?: stringResource(id = R.string.unknown_error),
                    showDismissButton = !messageState.didFail(),
                    onDismissRequest = resetMessageState,
                )
            }

            is Completed -> {
                messageState.response?.let {
                    if (it is ConfigNodeResetStatus) {
                        onBackPressed()
                    } else {
                        MeshMessageStatusDialog(
                            text = when (it) {
                                is StatusMessage -> it.message
                                else -> stringResource(id = R.string.label_success)
                            },
                            showDismissButton = messageState.didFail(),
                            onDismissRequest = resetMessageState,
                        )
                    }
                }
            }

            else -> {

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NodeInfo(
    node: Node,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onNameChanged: (String) -> Unit,
    onNetworkKeysClicked: (UUID) -> Unit,
    onApplicationKeysClicked: (UUID) -> Unit,
    onElementsClicked: (Address) -> Unit,
    onGetTtlClicked: () -> Unit,
    onProxyStateToggled: (Boolean) -> Unit,
    onGetProxyStateClicked: () -> Unit,
    onExcluded: (Boolean) -> Unit,
    onResetClicked: () -> Unit
) {
    val state = rememberPullToRefreshState()
    val scrollState = rememberScrollState()
    PullToRefreshBox(
        modifier = Modifier
            .fillMaxSize(),
        state = state,
        onRefresh = onRefresh,
        isRefreshing = isRefreshing
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = scrollState)
        ) {
            Spacer(modifier = Modifier.size(8.dp))
            NodeNameRow(name = node.name, onNameChanged = onNameChanged)
            SectionTitle(title = stringResource(id = R.string.title_keys))
            NetworkKeysRow(
                count = node.networkKeys.size,
                onNetworkKeysClicked = { onNetworkKeysClicked(node.uuid) }
            )
            Spacer(modifier = Modifier.size(8.dp))
            ApplicationKeysRow(
                count = node.applicationKeys.size,
                onApplicationKeysClicked = {
                    onApplicationKeysClicked(node.uuid)
                }
            )
            SectionTitle(title = stringResource(id = R.string.title_elements))
            node.elements.forEachIndexed { index, element ->
                ElementRow(
                    element = element,
                    onElementsClicked = {
                        onElementsClicked(element.unicastAddress.address)
                    }
                )
                if (index != node.elements.size - 1) Spacer(modifier = Modifier.size(8.dp))
            }
            SectionTitle(title = stringResource(id = R.string.title_node_information))
            NodeInformationRow(node)
            SectionTitle(title = stringResource(id = R.string.title_time_to_live))
            DefaultTtlRow(ttl = node.defaultTTL, onGetTtlClicked = onGetTtlClicked)
            SectionTitle(title = stringResource(id = R.string.title_proxy_state))
            ProxyStateRow(
                proxy = node.features.proxy,
                onProxyStateToggled = onProxyStateToggled,
                onGetProxyStateClicked = onGetProxyStateClicked
            )
            SectionTitle(title = stringResource(id = R.string.title_exclusions))
            ExclusionRow(isExcluded = node.excluded, onExcluded = onExcluded)
            SectionTitle(title = stringResource(id = R.string.label_reset_node))
            ResetRow(onResetClicked = onResetClicked)
            Spacer(modifier = Modifier.size(8.dp))
        }
    }
}

@Composable
private fun NodeNameRow(name: String, onNameChanged: (String) -> Unit) {
    ElevatedCardItemTextField(
        modifier = Modifier.padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.Badge,
        title = stringResource(id = R.string.label_name),
        subtitle = name,
        placeholder = stringResource(id = R.string.label_placeholder_node_name),
        onValueChanged = onNameChanged
    )
}


@Composable
private fun NetworkKeysRow(count: Int, onNetworkKeysClicked: () -> Unit) {
    ElevatedCardItem(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable(onClick = onNetworkKeysClicked),
        imageVector = Icons.Outlined.VpnKey,
        title = stringResource(R.string.label_network_keys),
        subtitle = "$count ${if (count == 1) "key" else "keys"} added"
    )
}

@Composable
private fun ApplicationKeysRow(count: Int, onApplicationKeysClicked: () -> Unit) {
    ElevatedCardItem(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable(onClick = onApplicationKeysClicked),
        imageVector = Icons.Outlined.VpnKey,
        title = stringResource(R.string.label_application_keys),
        subtitle = "$count ${if (count == 1) "key" else "keys"} added"
    )
}

@Composable
private fun ElementRow(element: Element, onElementsClicked: () -> Unit) {
    ElevatedCardItem(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable(onClick = onElementsClicked),
        imageVector = Icons.Outlined.DeviceHub,
        title = element.name ?: "Unknown",
        subtitle = "${element.models.size} ${if (element.models.size == 1) "model" else "models"}"
    )
}

@Composable
private fun NodeInformationRow(node: Node) {
    CompanyIdentifier(companyIdentifier = node.companyIdentifier)
    Spacer(modifier = Modifier.size(8.dp))
    ProductIdentifier(productIdentifier = node.productIdentifier)
    Spacer(modifier = Modifier.size(8.dp))
    ProductVersion(productVersion = node.versionIdentifier)
    Spacer(modifier = Modifier.size(8.dp))
    ReplayProtectionCount(replayProtectionCount = node.replayProtectionCount)
    Spacer(modifier = Modifier.size(8.dp))
    Security(node)
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
private fun CompanyIdentifier(companyIdentifier: UShort?) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.Factory,
        title = stringResource(R.string.label_company_identifier),
        subtitle = companyIdentifier?.toHexString()?.uppercase() ?: stringResource(R.string.unknown),
    )
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
private fun ProductIdentifier(productIdentifier: UShort?) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.QrCode,
        title = stringResource(R.string.label_product_identifier),
        subtitle = productIdentifier?.toHexString()?.uppercase() ?: stringResource(R.string.unknown),
    )
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
private fun ProductVersion(productVersion: UShort?) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.Numbers,
        title = stringResource(R.string.label_product_version),
        subtitle = productVersion?.toHexString()?.uppercase() ?: stringResource(R.string.unknown),
    )
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
private fun ReplayProtectionCount(replayProtectionCount: UShort?) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.SafetyCheck,
        title = stringResource(R.string.label_replay_protection_count),
        subtitle = replayProtectionCount?.toHexString()?.uppercase() ?: stringResource(R.string.unknown),
    )
}

@Composable
private fun Security(node: Node) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.Factory,
        title = stringResource(R.string.label_security),
        subtitle = when (node.elements.isEmpty()) {
            true -> node.security.toString()
            false -> stringResource(id = R.string.unknown)
        }
    )
}

@Composable
private fun DefaultTtlRow(ttl: UByte?, onGetTtlClicked: () -> Unit) {
    ElevatedCardItem(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable(onClick = onGetTtlClicked),
        imageVector = Icons.Outlined.Timer,
        title = stringResource(R.string.label_default_time_to_live),
        subtitle = if (ttl != null) "TTL set to $ttl" else "Unknown",
        supportingText = stringResource(R.string.label_default_ttl_rationale)
    ) {
        OutlinedButton(onClick = onGetTtlClicked) {
            Text(text = stringResource(R.string.label_get_ttl))
        }
        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
        OutlinedButton(onClick = {}) {
            Text(text = stringResource(R.string.label_set_ttl))
        }
    }
}

@Composable
private fun ProxyStateRow(
    proxy: Proxy?, onProxyStateToggled: (Boolean) -> Unit, onGetProxyStateClicked: () -> Unit
) {
    var enabled by rememberSaveable {
        mutableStateOf(proxy?.state?.let { it == FeatureState.Enabled } ?: false)
    }
    var showProxyStateDialog by rememberSaveable { mutableStateOf(false) }
    ElevatedCardItem(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable(onClick = onGetProxyStateClicked),
        imageVector = Icons.Outlined.Hub,
        title = stringResource(R.string.label_gatt_proxy_state),
        titleAction = {
            SwitchWithIcon(isChecked = enabled, onCheckedChange = {
                enabled = it
                if (!it) {
                    showProxyStateDialog = !showProxyStateDialog
                } else {
                    onProxyStateToggled(true)
                }
            })
        },
        subtitle = "Proxy state is ${if (enabled) "enabled" else "disabled"}",
        supportingText = stringResource(R.string.label_proxy_state_rationale)
    ) {
        OutlinedButton(onClick = onGetProxyStateClicked) {
            Text(text = stringResource(R.string.label_get_state))
        }
    }
    if (showProxyStateDialog) {
        MeshAlertDialog(onDismissRequest = {
            showProxyStateDialog = !showProxyStateDialog
            enabled = proxy?.state?.let { it == FeatureState.Enabled } ?: false
        },
            icon = Icons.Outlined.Hub,
            title = stringResource(R.string.label_disable_proxy_feature),
            text = stringResource(R.string.label_are_you_sure_rationale),
            iconColor = Color.Red,
            onConfirmClick = {
                enabled = false
                onProxyStateToggled(false)
                showProxyStateDialog = !showProxyStateDialog
            },
            onDismissClick = {
                showProxyStateDialog = !showProxyStateDialog
                enabled = proxy?.state?.let { it == FeatureState.Enabled } ?: false
            })
    }
}

@Composable
private fun ExclusionRow(isExcluded: Boolean, onExcluded: (Boolean) -> Unit) {
    var excluded by rememberSaveable { mutableStateOf(isExcluded) }
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.Block,
        title = stringResource(R.string.label_exclude_node),
        titleAction = {
            SwitchWithIcon(isChecked = isExcluded, onCheckedChange = {
                excluded = it
                onExcluded(it)
            })
        },
        subtitle = when (excluded) {
            true -> stringResource(id = R.string.label_node_excluded)
            false -> stringResource(id = R.string.label_node_not_excluded)
        },
        supportingText = stringResource(R.string.label_exclusion_rationale)
    )
}

@Composable
private fun ResetRow(onResetClicked: () -> Unit) {
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.Recycling,
        title = stringResource(R.string.label_reset_node),
        supportingText = stringResource(R.string.label_reset_node_rationale)
    ) {
        OutlinedButton(
            onClick = { showResetDialog = !showResetDialog },
            border = BorderStroke(width = 1.dp, color = Color.Red)
        ) {
            Text(text = stringResource(R.string.label_reset), color = Color.Red)
        }
    }
    if (showResetDialog) {
        MeshAlertDialog(onDismissRequest = { showResetDialog = !showResetDialog },
            icon = Icons.Outlined.Recycling,
            title = stringResource(R.string.label_reset_node),
            text = stringResource(R.string.label_are_you_sure_rationale),
            iconColor = Color.Red,
            onDismissClick = { showResetDialog = !showResetDialog },
            onConfirmClick = {
                showResetDialog = !showResetDialog
                onResetClicked()
            }
        )
    }
}