package no.nordicsemi.android.nrfmesh.feature.configurationserver

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.outlined.Start
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.ui.view.NordicAppBar
import no.nordicsemi.android.common.ui.view.NordicSliderDefaults
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.MeshSingleLineListItem
import no.nordicsemi.android.nrfmesh.core.ui.MeshTwoLineListItem
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigHeartbeatSubscriptionGet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigHeartbeatSubscriptionSet
import no.nordicsemi.kotlin.mesh.core.model.AllFriends
import no.nordicsemi.kotlin.mesh.core.model.AllNodes
import no.nordicsemi.kotlin.mesh.core.model.AllProxies
import no.nordicsemi.kotlin.mesh.core.model.AllRelays
import no.nordicsemi.kotlin.mesh.core.model.HeartbeatSubscription
import no.nordicsemi.kotlin.mesh.core.model.HeartbeatSubscriptionDestination
import no.nordicsemi.kotlin.mesh.core.model.HeartbeatSubscriptionSource
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.UnassignedAddress
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HeartBeatSubscriptionContent(
    model: Model,
    subscription: HeartbeatSubscription?,
    send: (AcknowledgedConfigMessage) -> Unit
) {
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    ElevatedCardItem(
        modifier = Modifier
            .padding(top = 8.dp)
            .padding(horizontal = 16.dp),
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
            sheetState = bottomSheetState,
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
                    onNavigationButtonClick = {
                        scope
                            .launch { bottomSheetState.hide() }
                            .invokeOnCompletion {
                                if (!bottomSheetState.isVisible) {
                                    showBottomSheet = false
                                }
                            }
                    },
                    backButtonIcon = Icons.Outlined.Close,
                    actions = {
                        IconButton(
                            onClick = { send(ConfigHeartbeatSubscriptionSet()) },
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
                        .fillMaxWidth()
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
        modifier = Modifier.padding(horizontal = 16.dp),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SourceRow(model: Model, subscription: HeartbeatSubscription?) {
    val network = model.parentElement?.parentNode?.network
    val sources = model.heartbeatSubscriptionSources()
    var expanded by rememberSaveable { mutableStateOf(false) }
    var selectedAddress by remember {
        mutableStateOf(UnassignedAddress as HeartbeatSubscriptionSource)
    }

    SectionTitle(title = stringResource(R.string.label_source))
    ExposedDropdownMenuBox(
        modifier = Modifier.padding(horizontal = 16.dp),
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        ElevatedCardItem(
            modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable),
            onClick = { expanded = true },
            imageVector = Icons.Outlined.Start,
            title = stringResource(R.string.label_source),
            titleAction = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            subtitle = if (selectedAddress is UnassignedAddress)
                stringResource(R.string.label_select_address)
            else network?.node(address = selectedAddress.address)?.name
                ?: selectedAddress.toHexString(),
        )
        DropdownMenu(
            modifier = Modifier.exposedDropdownSize(),
            expanded = expanded,
            onDismissRequest = { expanded = !expanded },
            content = {
                sources.forEachIndexed { index, source ->
                    DropdownMenuItem(
                        text = {
                            MeshSingleLineListItem(
                                leadingComposable = {
                                    Icon(
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp)
                                            .padding(end = 8.dp),
                                        imageVector = Icons.Outlined.Start,
                                        contentDescription = null
                                    )
                                },
                                title = network
                                    ?.node(address = source.address)?.name
                                    ?: source.toHexString()
                            )
                        },
                        onClick = {
                            selectedAddress = source
                            expanded = !expanded
                        }
                    )
                    if (index < sources.size - 1) {
                        HorizontalDivider()
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DestinationRow(model: Model, subscription: HeartbeatSubscription?) {
    val network = model.parentElement?.parentNode?.network
    val destinations = model.heartbeatPublicationDestinations()
    var expanded by rememberSaveable { mutableStateOf(false) }
    var selectedAddress by remember { mutableStateOf(UnassignedAddress as HeartbeatSubscriptionDestination) }

    SectionTitle(title = stringResource(R.string.label_destination))
    ExposedDropdownMenuBox(
        modifier = Modifier.padding(horizontal = 16.dp),
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        ElevatedCardItem(
            modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable),
            onClick = { expanded = true },
            imageVector = Icons.Outlined.SportsScore,
            title = stringResource(R.string.label_destination),
            titleAction = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            subtitle = if (selectedAddress is UnassignedAddress)
                stringResource(R.string.label_select_address)
            else network?.node(address = selectedAddress.address)?.name
                ?: selectedAddress.toHexString(),
        )
        DropdownMenu(
            modifier = Modifier.exposedDropdownSize(),
            expanded = expanded,
            onDismissRequest = { expanded = !expanded },
            content = {
                destinations.forEachIndexed { index, destination ->
                    DropdownMenuItem(
                        text = {
                            MeshSingleLineListItem(
                                leadingComposable = {
                                    Icon(
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp)
                                            .padding(end = 8.dp),
                                        imageVector = Icons.Outlined.SportsScore,
                                        contentDescription = null
                                    )
                                },
                                title = network
                                    ?.node(address = destination.address)
                                    ?.name ?: destination.toHexString(),
                            )
                        },
                        onClick = {
                            selectedAddress = destination
                            expanded = !expanded
                        }
                    )
                    if (index < destinations.size - 1) {
                        HorizontalDivider()
                    }
                }
            }
        )
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