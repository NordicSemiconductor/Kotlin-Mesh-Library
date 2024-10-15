package no.nordicsemi.android.nrfmesh.feature.configurationserver

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.SportsScore
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.ui.view.NordicAppBar
import no.nordicsemi.android.common.ui.view.NordicSliderDefaults
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.MeshTwoLineListItem
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigHeartbeatSubscriptionGet
import no.nordicsemi.kotlin.mesh.core.model.AllFriends
import no.nordicsemi.kotlin.mesh.core.model.AllNodes
import no.nordicsemi.kotlin.mesh.core.model.AllProxies
import no.nordicsemi.kotlin.mesh.core.model.AllRelays
import no.nordicsemi.kotlin.mesh.core.model.GroupAddress
import no.nordicsemi.kotlin.mesh.core.model.HeartbeatSubscription
import no.nordicsemi.kotlin.mesh.core.model.HeartbeatSubscriptionDestination
import no.nordicsemi.kotlin.mesh.core.model.HeartbeatSubscriptionSource
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.UnassignedAddress
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HeartBeatSubscriptionContent(
    model: Model,
    subscription: HeartbeatSubscription?,
    send: (AcknowledgedConfigMessage) -> Unit
) {
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    ElevatedCardItem(
        modifier = Modifier
            .padding(top = 8.dp)
            .padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.Forum,
        title = stringResource(R.string.label_subscriptions),
        subtitle = "Heartbeat subscriptions are ${
            if (subscription == null ||
                subscription.source is UnassignedAddress ||
                subscription.destination is UnassignedAddress
            ) "disabled" else "enabled"
        }",
        body = {
            if (subscription != null &&
                subscription.source !is UnassignedAddress &&
                subscription.destination !is UnassignedAddress
            ) {
                Spacer(modifier = Modifier.size(16.dp))
                MeshTwoLineListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height = 50.dp)
                        .padding(horizontal = 40.dp),
                    title = stringResource(R.string.label_source),
                    subtitle = with(subscription.source) {
                        model.parentElement?.parentNode?.network?.node(address)?.name
                            ?: toHexString()
                    }
                )
                MeshTwoLineListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height = 50.dp)
                        .padding(horizontal = 40.dp),
                    title = stringResource(R.string.label_destination),
                    subtitle = with(subscription.destination) {
                        model.parentElement?.parentNode?.network?.node(address)?.name
                            ?: toHexString()
                    }
                )
                MeshTwoLineListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height = 50.dp)
                        .padding(horizontal = 40.dp),
                    title = stringResource(R.string.label_remaining_period),
                    subtitle = when {
                        subscription.state != null -> subscription.state?.period.toString()
                        else -> stringResource(R.string.label_unknown)
                    }
                )
                MeshTwoLineListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height = 50.dp)
                        .padding(horizontal = 40.dp),
                    title = stringResource(R.string.label_count),
                    subtitle = when {
                        subscription.state != null -> subscription.state?.maxHops.toString()
                        else -> stringResource(R.string.label_unknown)
                    }
                )
                MeshTwoLineListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height = 50.dp)
                        .padding(horizontal = 40.dp),
                    title = stringResource(R.string.label_min_hops),
                    subtitle = when {
                        subscription.state != null -> subscription.state?.minHops.toString()
                        else -> stringResource(R.string.label_unknown)
                    }
                )
                MeshTwoLineListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height = 50.dp)
                        .padding(horizontal = 40.dp),
                    title = stringResource(R.string.label_max_hops),
                    subtitle = when {
                        subscription.state != null -> subscription.state?.maxHops.toString()
                        else -> stringResource(R.string.label_unknown)
                    }
                )
            }
        },
        actions = {
            OutlinedButton(
                onClick = { showBottomSheet = true },
                content = { Text(text = stringResource(R.string.label_set_state)) }
            )
            OutlinedButton(
                modifier = Modifier.padding(start = 8.dp),
                onClick = { send(ConfigHeartbeatSubscriptionGet()) },
                content = { Text(text = stringResource(R.string.label_get_state)) }
            )
        }
    )

    if (showBottomSheet) {
        ModalBottomSheet(
            containerColor = MaterialTheme.colorScheme.surface,
            sheetState = sheetState,
            onDismissRequest = {
                showBottomSheet = !showBottomSheet
            },
            dragHandle = {
                NordicAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.label_heartbeat_subscription),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    onNavigationButtonClick = { showBottomSheet = !showBottomSheet },
                    backButtonIcon = Icons.Outlined.Close,
                    actions = {
                        IconButton(
                            onClick = { showBottomSheet = !showBottomSheet },
                            content = {
                                Icon(
                                    imageVector = Icons.Outlined.Save,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                )
            },
            content = {
                Column(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxSize()
                        .verticalScroll(state = rememberScrollState())
                ) {
                    PeriodRow(subscription = subscription)
                    SourceRow(model = model, subscription = subscription)
                    DestinationRow(model = model, subscription = subscription)
                }
            }
        )
    }
}

@Composable
private fun PeriodRow(subscription: HeartbeatSubscription?) {
    var periodLog by remember {
        mutableFloatStateOf(subscription?.state?.periodLog?.toFloat() ?: 0f)
    }
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.Timer,
        title = stringResource(R.string.label_period),
        subtitle = "Remaining period in seconds",
        body = {
            Slider(
                modifier = Modifier.padding(start = 40.dp),
                value = periodLog,
                onValueChange = { periodLog = it },
                valueRange = HeartbeatSubscription.PERIOD_LOG_MIN.toFloat()..HeartbeatSubscription.PERIOD_LOG_MAX.toFloat(),
                steps = 16,
                colors = NordicSliderDefaults.colors()
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
                    .sizeIn(minWidth = 80.dp),
                text = stringResource(
                    R.string.label_heartbeat_subscription_period_seconds,
                    HeartbeatSubscription.periodLog2Period(
                        periodLog = periodLog.roundToInt().toUByte()
                    )
                ),
                textAlign = TextAlign.End
            )
        }
    )
}

@Composable
private fun SourceRow(model: Model, subscription: HeartbeatSubscription?) {
    val network = model.parentElement?.parentNode?.network
    val sources = model.heartbeatSubscriptionSources()
    var selectedIndex by rememberSaveable { mutableIntStateOf(-1) }
    var selectedAddressIndex by rememberSaveable { mutableIntStateOf(-1) }

    SectionTitle(title = stringResource(R.string.label_source))
    sources.forEachIndexed { index, source ->
        ElevatedCardItem(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .clickable {
                    selectedAddressIndex = -1
                    selectedIndex = index
                },
            imageVector = Icons.Outlined.SportsScore,
            title = network
                ?.node(address = source.address)
                ?.name ?: source.toHexString(),
            titleAction = {
                RadioButton(
                    selected = selectedIndex == index,
                    onClick = {
                        selectedAddressIndex = -1
                        selectedIndex = index
                    }
                )
            }
        )
        if (index < sources.size - 1) {
            Spacer(modifier = Modifier.size(8.dp))
        }
    }
}

@Composable
private fun DestinationRow(model: Model, subscription: HeartbeatSubscription?) {
    val network = model.parentElement?.parentNode?.network
    val destinations = model.heartbeatPublicationDestinations()
    var selectedIndex by rememberSaveable { mutableIntStateOf(-1) }
    var selectedAddressIndex by rememberSaveable { mutableIntStateOf(-1) }

    SectionTitle(title = stringResource(R.string.label_destination))
    destinations.forEachIndexed { index, destination ->
        ElevatedCardItem(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .clickable {
                    selectedAddressIndex = -1
                    selectedIndex = index
                },
            imageVector = Icons.Outlined.SportsScore,
            title = when (destination) {
                is UnicastAddress -> network
                    ?.node(address = destination.address)
                    ?.name ?: destination.toHexString()

                is AllRelays -> "All Relays"
                is AllFriends -> "All Friends"
                is AllProxies -> "All Proxies"
                is AllNodes -> "All Nodes"
                is GroupAddress -> network?.group(address = destination.address)?.name
                    ?: destination.toHexString()

                is UnassignedAddress -> "Unassigned"
            },
            titleAction = {
                RadioButton(
                    selected = selectedIndex == index,
                    onClick = {
                        selectedAddressIndex = -1
                        selectedIndex = index
                    }
                )
            }
        )
        if (index < destinations.size - 1) {
            Spacer(modifier = Modifier.size(8.dp))
        }
    }
}

/**
 * Returns the list of possible addresses that can be selected as a source address for the Heartbeat
 * subscription messages for a give ConfigurationServer Model
 */
private fun Model.heartbeatSubscriptionSources(): List<HeartbeatSubscriptionSource> {
    require(isConfigurationServer) { throw IllegalStateException("Model is not a Configuration Server") }
    val parentNode = parentElement?.parentNode
    return parentNode?.network?.nodes?.filter {
        it != parentNode
    }?.map {
        it.primaryUnicastAddress
    } ?: emptyList()
}

/**
 * Returns the list of possible addresses that can be selected as a destination address for the
 * Heartbeat subscription messages for a given ConfigurationServer Model.
 */
private fun Model.heartbeatPublicationDestinations(): List<HeartbeatSubscriptionDestination> {
    require(isConfigurationServer) { throw IllegalStateException("Model is not a Configuration Server") }
    val destination = parentElement?.parentNode?.primaryUnicastAddress
    return listOf(
        destination as HeartbeatSubscriptionDestination,
        AllRelays as HeartbeatSubscriptionDestination,
        AllFriends as HeartbeatSubscriptionDestination,
        AllProxies as HeartbeatSubscriptionDestination,
        AllNodes as HeartbeatSubscriptionDestination
    )
}