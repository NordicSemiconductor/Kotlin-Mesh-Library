package no.nordicsemi.android.nrfmesh.feature.nodes

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.DeviceHub
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.android.nrfmesh.core.ui.MeshOutlinedTextField
import no.nordicsemi.android.nrfmesh.core.ui.MeshTwoLineListItem
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.core.ui.SwitchWithIcon
import no.nordicsemi.kotlin.mesh.core.model.Element
import no.nordicsemi.kotlin.mesh.core.model.Node

@Composable
fun NodeRoute(
    uiState: NodeScreenUiState,
    onNameChanged: (String) -> Unit,
    onNetworkKeysClicked: () -> Unit,
    onApplicationKeysClicked: () -> Unit,
    onElementsClicked: () -> Unit,
    onGetTtlClicked: () -> Unit,
    onGetProxyStateClicked: () -> Unit,
    onExcluded: (Boolean) -> Unit,
    onResetClicked: () -> Unit
) {
    NodeScreen(
        nodeState = uiState.nodeState,
        onNameChanged = onNameChanged,
        onNetworkKeysClicked = onNetworkKeysClicked,
        onApplicationKeysClicked = onApplicationKeysClicked,
        onElementsClicked = onElementsClicked,
        onGetTtlClicked = onGetTtlClicked,
        onGetProxyStateClicked = onGetProxyStateClicked,
        onExcluded = onExcluded,
        onResetClicked = onResetClicked
    )
}

@Composable
private fun NodeScreen(
    nodeState: NodeState,
    onNameChanged: (String) -> Unit,
    onNetworkKeysClicked: () -> Unit,
    onApplicationKeysClicked: () -> Unit,
    onElementsClicked: () -> Unit,
    onGetTtlClicked: () -> Unit,
    onGetProxyStateClicked: () -> Unit,
    onExcluded: (Boolean) -> Unit,
    onResetClicked: () -> Unit
) {
    LazyColumn(modifier = Modifier.padding(vertical = 16.dp)) {
        when (nodeState) {
            NodeState.Loading -> {}

            is NodeState.Success -> {
                nodeInfo(
                    node = nodeState.node,
                    onNameChanged = onNameChanged,
                    onNetworkKeysClicked = onNetworkKeysClicked,
                    onApplicationKeysClicked = onApplicationKeysClicked,
                    onElementsClicked = onElementsClicked,
                    onGetTtlClicked = onGetTtlClicked,
                    onGetProxyStateClicked = onGetProxyStateClicked,
                    onExcluded = onExcluded,
                    onResetClicked = onResetClicked
                )
            }

            is NodeState.Error -> {
                item {
                    MeshNoItemsAvailable(
                        imageVector = Icons.Outlined.ErrorOutline,
                        title = nodeState.throwable.message ?: "Unknown error"
                    )
                }
            }
        }
    }
}

private fun LazyListScope.nodeInfo(
    node: Node,
    onNameChanged: (String) -> Unit,
    onNetworkKeysClicked: () -> Unit,
    onApplicationKeysClicked: () -> Unit,
    onElementsClicked: () -> Unit,
    onGetTtlClicked: () -> Unit,
    onGetProxyStateClicked: () -> Unit,
    onExcluded: (Boolean) -> Unit,
    onResetClicked: () -> Unit
) {
    item { NodeNameRow(name = node.name, onNameChanged = onNameChanged) }
    item { SectionTitle(title = stringResource(id = R.string.title_keys)) }
    item {
        NetworkKeysRow(count = node.networkKeys.size, onNetworkKeysClicked = onNetworkKeysClicked)
        Spacer(modifier = Modifier.size(8.dp))
    }
    item {
        ApplicationKeysRow(
            count = node.applicationKeys.size,
            onApplicationKeysClicked = onApplicationKeysClicked
        )
    }
    item { SectionTitle(title = stringResource(id = R.string.title_elements)) }
    node.elements.forEachIndexed { index, element ->
        item {
            ElementRow(element = element, onElementsClicked = onElementsClicked)
            if(index != node.elements.size - 1)
                Spacer(modifier = Modifier.size(8.dp))
        }
    }
    item {
        SectionTitle(title = stringResource(id = R.string.title_time_to_live))
    }
    item { DefaultTtlRow(ttl = node.defaultTTL, onGetTtlClicked = onGetTtlClicked) }
    item { SectionTitle(title = stringResource(id = R.string.title_proxy_state)) }
    item { ProxyStateRow(isEnabled = false, onGetProxyStateClicked = onGetProxyStateClicked) }
    item { SectionTitle(title = stringResource(id = R.string.title_exclusions)) }
    item { ExclusionRow(isExcluded = node.excluded, onExcluded = onExcluded) }
    item { SectionTitle(title = stringResource(id = R.string.label_reset_node)) }
    item { ResetRow(onResetClicked = onResetClicked) }
}

@Composable
private fun NodeNameRow(name: String, onNameChanged: (String) -> Unit) {
    var value by rememberSaveable { mutableStateOf(name) }
    var onEditClick by rememberSaveable { mutableStateOf(false) }
    ElevatedCard(
        modifier = Modifier
            .padding(horizontal = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                modifier = Modifier.padding(all = 12.dp),
                imageVector = Icons.Outlined.Badge,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
            Crossfade(targetState = onEditClick, label = "NodeName") { state ->
                when (state) {
                    true -> MeshOutlinedTextField(
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
                        onFocus = onEditClick,
                        value = value,
                        onValueChanged = { value = it },
                        label = { Text(text = stringResource(id = R.string.label_name)) },
                        placeholder = {
                            Text(text = stringResource(id = R.string.label_placeholder_node_name))
                        },
                        internalTrailingIcon = {
                            IconButton(enabled = value.isNotBlank(), onClick = { value = "" }) {
                                Icon(imageVector = Icons.Outlined.Clear, contentDescription = null)
                            }
                        },
                        content = {
                            IconButton(
                                modifier = Modifier.padding(start = 8.dp, end = 16.dp),
                                enabled = value.isNotBlank(),
                                onClick = {
                                    onEditClick = !onEditClick
                                    value = value.trim()
                                    onNameChanged(value)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = LocalContentColor.current.copy(alpha = 0.6f)
                                )
                            }
                        }
                    )

                    false -> MeshTwoLineListItem(
                        title = stringResource(id = R.string.label_name),
                        subtitle = value,
                        trailingComposable = {
                            IconButton(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                onClick = {
                                    onEditClick = !onEditClick
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = null,
                                    tint = LocalContentColor.current.copy(alpha = 0.6f)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}


@Composable
private fun NetworkKeysRow(count: Int, onNetworkKeysClicked: () -> Unit) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.VpnKey,
        title = stringResource(R.string.label_network_keys),
        subtitle = "$count ${if (count == 1) "key" else "keys"} added"
    )
}

@Composable
private fun ApplicationKeysRow(count: Int, onApplicationKeysClicked: () -> Unit) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 8.dp),
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
private fun DefaultTtlRow(ttl: UByte?, onGetTtlClicked: () -> Unit) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 8.dp),
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
private fun ProxyStateRow(isEnabled: Boolean, onGetProxyStateClicked: () -> Unit) {
    var enabled by rememberSaveable { mutableStateOf(isEnabled) }
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.Hub,
        title = stringResource(R.string.label_gatt_proxy_state),
        titleAction = { SwitchWithIcon(isChecked = enabled, onCheckedChange = { enabled = it }) },
        subtitle = "Proxy state is ${if (enabled) "enabled" else "disabled"}",
        supportingText = stringResource(R.string.label_proxy_state_rationale)
    ) {
        OutlinedButton(onClick = onGetProxyStateClicked) {
            Text(text = stringResource(R.string.label_get_state))
        }
    }
}

@Composable
private fun ExclusionRow(isExcluded: Boolean, onExcluded: (Boolean) -> Unit) {
    var excluded by rememberSaveable { mutableStateOf(isExcluded) }
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.Block,
        title = stringResource(R.string.label_exclude_node),
        titleAction = { SwitchWithIcon(isChecked = excluded, onCheckedChange = { excluded = it }) },
        subtitle = "Node is ${if (excluded) "excluded" else "not excluded"} from the network",
        supportingText = stringResource(R.string.label_exclusion_rationale)
    )
}

@Composable
private fun ResetRow(onResetClicked: () -> Unit) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.LockReset,
        title = stringResource(R.string.label_reset_node),
        supportingText = stringResource(R.string.label_reset_node_rationale)
    ) {
        OutlinedButton(
            onClick = onResetClicked,
            border = BorderStroke(width = 1.dp, color = Color.Red)
        ) {
            Text(text = stringResource(R.string.label_reset), color = Color.Red)
        }
    }
}