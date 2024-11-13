package no.nordicsemi.android.nrfmesh.feature.model.configurationServer

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Diversity1
import androidx.compose.material.icons.outlined.Groups3
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.WavingHand
import androidx.compose.material.icons.outlined.WifiTethering
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.ui.view.NordicSliderDefaults
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.core.common.NotStarted.isInProgress
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.MeshAlertDialog
import no.nordicsemi.android.nrfmesh.core.ui.MeshSingleLineListItem
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.feature.configurationserver.R
import no.nordicsemi.android.nrfmesh.feature.model.NodeIdentityStatus
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigBeaconGet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigBeaconSet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigFriendGet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigFriendSet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigGattProxyGet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigGattProxySet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNetworkTransmitGet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNetworkTransmitSet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNodeIdentitySet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigRelayGet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigRelaySet
import no.nordicsemi.kotlin.mesh.core.model.FeatureState
import no.nordicsemi.kotlin.mesh.core.model.Friend
import no.nordicsemi.kotlin.mesh.core.model.HeartbeatPublication
import no.nordicsemi.kotlin.mesh.core.model.HeartbeatSubscription
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.NetworkTransmit
import no.nordicsemi.kotlin.mesh.core.model.NodeIdentityState
import no.nordicsemi.kotlin.mesh.core.model.Proxy
import no.nordicsemi.kotlin.mesh.core.model.Relay
import no.nordicsemi.kotlin.mesh.core.model.RelayRetransmit
import kotlin.math.roundToInt

@Composable
internal fun ConfigurationServerModel(
    model: Model,
    messageState: MessageState,
    nodeIdentityStates: List<NodeIdentityStatus>,
    send: (AcknowledgedConfigMessage) -> Unit,
    requestNodeIdentityStates: () -> Unit
) {
    RelayFeature(
        messageState = messageState,
        relayRetransmit = model.parentElement?.parentNode?.relayRetransmit,
        relay = model.parentElement?.parentNode?.features?.relay,
        send = send
    )
    NetworkTransmit(
        messageState = messageState,
        networkTransmit = model.parentElement?.parentNode?.networkTransmit,
        send = send
    )
    SecureNetworkBeacon(
        messageState = messageState,
        friend = model.parentElement?.parentNode?.features?.friend,
        send = send
    )
    FriendFeature(
        messageState = messageState,
        friend = model.parentElement?.parentNode?.features?.friend,
        send = send
    )
    ProxyStateRow(
        messageState = messageState,
        proxy = model.parentElement?.parentNode?.features?.proxy,
        send = send
    )
    NodeIdentityRow(
        nodeIdentityStates = nodeIdentityStates,
        send = send,
        requestNodeIdentityStates = requestNodeIdentityStates
    )
    SectionTitle(stringResource(id = R.string.title_heartbeat))
    HeartBeatSubscriptionRow(
        model = model,
        subscription = model.parentElement?.parentNode?.heartbeatSubscription,
        send = send
    )
    HeartBeatPublicationRow(
        model = model,
        publication = model.parentElement?.parentNode?.heartbeatPublication,
        send = send
    )
}

@Composable
private fun RelayFeature(
    messageState: MessageState,
    relayRetransmit: RelayRetransmit?,
    relay: Relay?,
    send: (AcknowledgedConfigMessage) -> Unit
) {
    var retransmissions by remember {
        mutableFloatStateOf(relayRetransmit?.count?.toFloat() ?: 0f)
    }
    var interval by remember {
        mutableFloatStateOf(relayRetransmit?.interval?.toFloat() ?: 0f)
    }
    ElevatedCardItem(
        modifier = Modifier
            .padding(top = 8.dp)
            .padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Groups3,
        title = stringResource(R.string.title_relay_count_and_interval),
        body = {
            Slider(
                enabled = relay?.state?.isSupported == true && !messageState.isInProgress(),
                value = retransmissions,
                onValueChange = {
                    retransmissions = it
                },
                valueRange = RelayRetransmit.COUNT_RANGE.toFloat(),
                steps = 6,
                colors = NordicSliderDefaults.colors()
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
                    .sizeIn(minWidth = 80.dp),
                text = when (relayRetransmit) {
                    null -> "Unknown"
                    else -> "${retransmissions.roundToInt()} transmission(s)"
                },
                textAlign = TextAlign.End
            )
            Slider(
                enabled = relay?.state?.isSupported == true &&
                        retransmissions > 0 &&
                        !messageState.isInProgress(),
                value = interval,
                onValueChange = { interval = it },
                valueRange = RelayRetransmit.INTERVAL_RANGE.toFloat(),
                steps = 30,
                colors = NordicSliderDefaults.colors()
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
                    .sizeIn(minWidth = 80.dp),
                text = when (relayRetransmit) {
                    null -> "Unknown"
                    else -> "${interval.roundToInt()} ms"
                },
                textAlign = TextAlign.End
            )
        },
        actions = {
            OutlinedButton(
                enabled = !messageState.isInProgress(),
                onClick = { send(ConfigRelayGet()) },
                content = { Text(text = stringResource(R.string.label_get_state)) }
            )
            OutlinedButton(
                modifier = Modifier.padding(start = 8.dp),
                enabled = !messageState.isInProgress(),
                onClick = {
                    send(
                        ConfigRelaySet(
                            relayRetransmit = RelayRetransmit(
                                count = retransmissions.roundToInt(),
                                interval = interval.roundToInt()
                            )
                        )
                    )
                },
                content = { Text(text = stringResource(R.string.label_set_relay)) }
            )
        }
    )
}

@Composable
private fun NetworkTransmit(
    messageState: MessageState,
    networkTransmit: NetworkTransmit?,
    send: (AcknowledgedConfigMessage) -> Unit
) {
    var transmissions by remember {
        mutableFloatStateOf(networkTransmit?.count?.toFloat() ?: 0f)
    }
    var interval by remember {
        mutableFloatStateOf(networkTransmit?.interval?.toFloat() ?: 0f)
    }
    ElevatedCardItem(
        modifier = Modifier
            .padding(top = 8.dp)
            .padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Groups3,
        title = stringResource(R.string.title_network_transmit),
        body = {
            Slider(
                enabled = !messageState.isInProgress(),
                value = transmissions,
                onValueChange = {
                    transmissions = it
                },
                valueRange = RelayRetransmit.COUNT_RANGE.toFloat(),
                steps = 6,
                colors = NordicSliderDefaults.colors()
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
                    .sizeIn(minWidth = 80.dp),
                text = when (networkTransmit) {
                    null -> "Unknown"
                    else -> "${transmissions.roundToInt()} transmission(s)"
                },
                textAlign = TextAlign.End
            )
            Slider(
                enabled = transmissions > 0 && !messageState.isInProgress(),
                value = interval,
                onValueChange = { interval = it },
                valueRange = RelayRetransmit.INTERVAL_RANGE.toFloat(),
                steps = 30,
                colors = NordicSliderDefaults.colors()
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
                    .sizeIn(minWidth = 80.dp),
                text = when (networkTransmit) {
                    null -> "Unknown"
                    else -> "${interval.roundToInt()} ms"
                },
                textAlign = TextAlign.End
            )
        },
        actions = {
            OutlinedButton(
                enabled = !messageState.isInProgress(),
                onClick = {
                    send(ConfigNetworkTransmitGet())
                          },
                content = { Text(text = stringResource(R.string.label_get_state)) }
            )
            OutlinedButton(
                modifier = Modifier.padding(start = 8.dp),
                enabled = !messageState.isInProgress(),
                onClick = {
                    send(
                        ConfigNetworkTransmitSet(
                            count = transmissions.roundToInt().toUByte(),
                            steps = NetworkTransmit.toSteps(interval.roundToInt().toUShort())
                        )
                    )
                },
                content = { Text(text = stringResource(R.string.label_set_relay)) }
            )
        }
    )
}

@Composable
private fun SecureNetworkBeacon(
    messageState: MessageState,
    friend: Friend?,
    send: (AcknowledgedConfigMessage) -> Unit
) {
    ElevatedCardItem(
        modifier = Modifier
            .padding(top = 8.dp)
            .padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.WifiTethering,
        title = stringResource(R.string.label_snb),
        titleAction = {
            Switch(
                enabled = !messageState.isInProgress(),
                checked = friend?.state?.isEnabled ?: false,
                onCheckedChange = { send(ConfigBeaconSet(enable = it)) }
            )
        },
        subtitle = "SNB is ${
            if (friend?.state?.isEnabled == true) "enabled"
            else "disabled"
        }",
        supportingText = stringResource(R.string.label_snb_rationale)
    ) {
        OutlinedButton(
            enabled = !messageState.isInProgress(),
            onClick = { send(ConfigBeaconGet()) },
            content = { Text(text = stringResource(R.string.label_get_state)) }
        )
    }
}

@Composable
private fun FriendFeature(
    messageState: MessageState,
    friend: Friend?,
    send: (AcknowledgedConfigMessage) -> Unit
) {
    ElevatedCardItem(
        modifier = Modifier
            .padding(top = 8.dp)
            .padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Diversity1,
        title = stringResource(R.string.label_friend),
        titleAction = {
            Switch(
                enabled = !messageState.isInProgress(),
                checked = friend?.state?.isEnabled ?: false,
                onCheckedChange = { send(ConfigFriendSet(enable = it)) }
            )
        },
        subtitle = "Friend feature is ${
            if (friend?.state?.isEnabled == true) "enabled"
            else "disabled"
        }",
        supportingText = stringResource(R.string.label_friend_feature_rationale)
    ) {
        OutlinedButton(
            enabled = !messageState.isInProgress(),
            onClick = { send(ConfigFriendGet()) },
            content = { Text(text = stringResource(R.string.label_get_state)) }
        )
    }
}

@Composable
private fun ProxyStateRow(
    messageState: MessageState,
    proxy: Proxy?,
    send: (AcknowledgedConfigMessage) -> Unit
) {
    var showProxyStateDialog by rememberSaveable { mutableStateOf(false) }
    ElevatedCardItem(
        modifier = Modifier
            .padding(top = 8.dp)
            .padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Hub,
        title = stringResource(R.string.label_gatt_proxy),
        titleAction = {
            Switch(
                enabled = !messageState.isInProgress(),
                checked = proxy?.state == FeatureState.Enabled,
                onCheckedChange = {
                    when (!it) {
                        true -> showProxyStateDialog = !showProxyStateDialog
                        else -> send(ConfigGattProxySet(state = FeatureState.Enabled))
                    }
                }
            )
        },
        subtitle = "Proxy state is ${
            if (proxy?.state == FeatureState.Enabled) "enabled"
            else "disabled"
        }",
        supportingText = stringResource(R.string.label_proxy_state_rationale)
    ) {
        OutlinedButton(
            enabled = !messageState.isInProgress(),
            onClick = { send(ConfigGattProxyGet()) },
            content = { Text(text = stringResource(R.string.label_get_state)) }
        )
    }
    if (showProxyStateDialog) {
        MeshAlertDialog(onDismissRequest = {
            showProxyStateDialog = !showProxyStateDialog
        },
            icon = Icons.Outlined.Hub,
            title = stringResource(R.string.label_disable_proxy_feature),
            text = stringResource(R.string.label_are_you_sure_rationale),
            iconColor = Color.Red,
            onConfirmClick = {
                send(ConfigGattProxySet(state = FeatureState.Disabled))
                showProxyStateDialog = !showProxyStateDialog
            },
            onDismissClick = { showProxyStateDialog = !showProxyStateDialog }
        )
    }
}

@Composable
private fun NodeIdentityRow(
    nodeIdentityStates: List<NodeIdentityStatus>,
    send: (AcknowledgedConfigMessage) -> Unit,
    requestNodeIdentityStates: () -> Unit,
) {
    ElevatedCardItem(
        modifier = Modifier
            .padding(top = 8.dp)
            .padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.WavingHand,
        title = stringResource(R.string.label_node_identity),
        supportingText = stringResource(R.string.label_node_identity_rationale),
        body = {
            nodeIdentityStates.forEach { state ->
                NodeIdentityStatusRow(
                    networkKey = state.networkKey,
                    state = state.nodeIdentityState,
                    send = send
                )
            }
        },
        actions = {
            OutlinedButton(
                onClick = requestNodeIdentityStates,
                content = { Text(text = stringResource(R.string.label_get_state)) }
            )
        }
    )
}

@Composable
private fun NodeIdentityStatusRow(
    networkKey: NetworkKey,
    state: NodeIdentityState?,
    send: (AcknowledgedConfigMessage) -> Unit
) {
    MeshSingleLineListItem(
        modifier = Modifier.padding(start = 42.dp),
        title = networkKey.name,
        trailingComposable = {
            Switch(
                enabled = state?.isSupported ?: false,
                checked = state?.isSupported == true && state.isRunning,
                onCheckedChange = {
                    // isChecked = it
                    send(
                        ConfigNodeIdentitySet(
                            networkKeyIndex = networkKey.index,
                            start = it
                        )
                    )
                }
            )
        }
    )
}

@Composable
private fun HeartBeatSubscriptionRow(
    model: Model,
    subscription: HeartbeatSubscription?,
    send: (AcknowledgedConfigMessage) -> Unit
) {
    HeartBeatSubscriptionContent(
        model = model,
        subscription = subscription,
        send = send
    )
}

@Composable
private fun HeartBeatPublicationRow(
    model: Model,
    publication: HeartbeatPublication?,
    send: (AcknowledgedConfigMessage) -> Unit
) {
    HeartBeatPublicationContent(
        model = model,
        publication = publication,
        send = send
    )
}

fun IntRange.toFloat(): ClosedFloatingPointRange<Float> =
    start.toFloat()..endInclusive.toFloat()