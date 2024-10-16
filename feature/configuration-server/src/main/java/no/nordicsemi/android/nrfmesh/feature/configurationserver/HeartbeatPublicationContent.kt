package no.nordicsemi.android.nrfmesh.feature.configurationserver

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Groups3
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.SportsScore
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.VpnKey
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.ui.view.NordicAppBar
import no.nordicsemi.android.common.ui.view.NordicSliderDefaults
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItemTextField
import no.nordicsemi.android.nrfmesh.core.ui.MeshSingleLineListItem
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.kotlin.data.toByteArray
import no.nordicsemi.kotlin.data.toHexString
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigHeartbeatPublicationSet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigHeartbeatSubscriptionGet
import no.nordicsemi.kotlin.mesh.core.model.AllFriends
import no.nordicsemi.kotlin.mesh.core.model.AllNodes
import no.nordicsemi.kotlin.mesh.core.model.AllProxies
import no.nordicsemi.kotlin.mesh.core.model.AllRelays
import no.nordicsemi.kotlin.mesh.core.model.GroupAddress
import no.nordicsemi.kotlin.mesh.core.model.HeartbeatPublication
import no.nordicsemi.kotlin.mesh.core.model.HeartbeatPublicationDestination
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.RelayRetransmit
import no.nordicsemi.kotlin.mesh.core.model.UnassignedAddress
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HeartBeatPublicationContent(
    model: Model,
    publication: HeartbeatPublication?,
    send: (AcknowledgedConfigMessage) -> Unit
) {
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ElevatedCardItem(
        modifier = Modifier
            .padding(top = 8.dp, bottom = 16.dp)
            .padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Forum,
        title = stringResource(R.string.label_publications),
        subtitle = "Heartbeat publications are ${
            if (publication == null || publication.address is UnassignedAddress)
                "disabled"
            else "enabled"
        }",
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
            onDismissRequest = { showBottomSheet = !showBottomSheet },
            dragHandle = {
                NordicAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.label_heartbeat_publication),
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
                            // Note: If you provide logic outside of onDismissRequest to remove the
                            // sheet, you must additionally handle intended state cleanup, if any.
                            onClick = { send(ConfigHeartbeatPublicationSet()) },
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
                        .fillMaxSize()
                        .verticalScroll(state = rememberScrollState())
                ) {
                    SectionTitle(title = stringResource(R.string.label_network_key))
                    NetworkKeysRow(
                        network = model.parentElement?.parentNode?.network,
                        onNetworkKeySelected = {}
                    )
                    SectionTitle(title = stringResource(R.string.label_destination))
                    DestinationRow(
                        model = model,
                        publication = publication
                    )
                    SectionTitle(title = stringResource(R.string.label_time_to_live))
                    TtlRow(ttl = 5, onTtlChanged = {})
                    SectionTitle(title = stringResource(R.string.label_periodic_heartbeats))
                    PeriodicHeartbeatsRow(
                        publication = publication,
                        model = model
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NetworkKeysRow(
    network: MeshNetwork?,
    onNetworkKeySelected: (NetworkKey) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var selectedKeyIndex by rememberSaveable { mutableIntStateOf(0) }
    ExposedDropdownMenuBox(
        modifier = Modifier.padding(horizontal = 16.dp),
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        ElevatedCardItem(
            modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable),
            onClick = { expanded = true },
            imageVector = Icons.Outlined.VpnKey,
            title = stringResource(R.string.label_network_key),
            titleAction = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            subtitle = network?.networkKeys?.firstOrNull {
                it.index == selectedKeyIndex.toUShort()
            }?.name ?: stringResource(R.string.label_unknown)
        )
        DropdownMenu(
            modifier = Modifier.exposedDropdownSize(),
            expanded = expanded,
            onDismissRequest = { expanded = !expanded },
            content = {
                network?.networkKeys?.forEachIndexed { index, key ->
                    DropdownMenuItem(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        text = {
                            MeshSingleLineListItem(
                                leadingComposable = {
                                    Icon(
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp)
                                            .padding(end = 8.dp),
                                        imageVector = Icons.Outlined.VpnKey,
                                        contentDescription = null
                                    )
                                },
                                title = key.name,
                            )
                        },
                        onClick = {
                            selectedKeyIndex = key.index.toInt()
                            expanded = !expanded
                        }
                    )
                    if (index < network.networkKeys.size - 1) {
                        HorizontalDivider()
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalStdlibApi::class)
@Composable
private fun DestinationRow(model: Model, publication: HeartbeatPublication?) {
    val network = model.parentElement?.parentNode?.network
    val destinations = model.heartbeatPublicationDestinations()

    var expanded by rememberSaveable { mutableStateOf(false) }
    var selectedAddress by remember {
        mutableStateOf(UnassignedAddress as HeartbeatPublicationDestination)
    }
    ExposedDropdownMenuBox(
        modifier = Modifier.padding(horizontal = 16.dp),
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        ElevatedCardItem(
            modifier = Modifier
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable),
            onClick = { expanded = true },
            imageVector = Icons.Outlined.SportsScore,
            title = when (selectedAddress) {
                is UnicastAddress -> network
                    ?.node(address = selectedAddress.address)
                    ?.name ?: selectedAddress.toHexString()

                is AllRelays -> stringResource(R.string.label_all_relays)
                is AllFriends -> stringResource(R.string.label_all_friends)
                is AllProxies -> stringResource(R.string.label_all_proxies)
                is AllNodes -> stringResource(R.string.label_all_nodes)
                is GroupAddress -> network?.group(address = selectedAddress.address)?.name
                    ?: selectedAddress.toHexString()

                is UnassignedAddress -> stringResource(R.string.label_unassigned_address)
            },
            titleAction = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            subtitle = "0x${selectedAddress.address.toByteArray().toHexString()}"
        )
        DropdownMenu(
            modifier = Modifier.exposedDropdownSize(),
            expanded = expanded,
            onDismissRequest = { expanded = !expanded },
            content = {
                destinations.forEachIndexed { index, destination ->
                    DropdownMenuItem(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
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
                                title = when (destination) {
                                    is UnicastAddress -> network
                                        ?.node(address = destination.address)?.name
                                        ?: destination.toHexString()

                                    is AllRelays -> stringResource(R.string.label_all_relays)
                                    is AllFriends -> stringResource(R.string.label_all_friends)
                                    is AllProxies -> stringResource(R.string.label_all_proxies)
                                    is AllNodes -> stringResource(R.string.label_all_nodes)
                                    is GroupAddress -> network
                                        ?.group(address = destination.address)?.name
                                        ?: destination.toHexString()

                                    is UnassignedAddress -> stringResource(R.string.label_unassigned_address)
                                },
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

@Composable
private fun TtlRow(
    ttl: Int?,
    onTtlChanged: (Int) -> Unit,
) {
    ElevatedCardItemTextField(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Timer,
        title = stringResource(id = R.string.label_initial_ttl),
        subtitle = ttl?.toString() ?: "0",
        onValueChanged = { onTtlChanged(it.toInt()) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        )
    )
}

@Composable
private fun PeriodicHeartbeatsRow(
    model: Model,
    publication: HeartbeatPublication?
) {
    var countLog by remember {
        mutableFloatStateOf(publication?.count?.toFloat() ?: 0f)
    }
    var period by remember {
        mutableFloatStateOf(publication?.period?.toFloat() ?: 0f)
    }
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Groups3,
        title = stringResource(R.string.title_heartbeat_count_and_period),
        body = {
            Slider(
                value = countLog,
                onValueChange = { countLog = it },
                valueRange = RelayRetransmit.COUNT_RANGE.toFloat(),
                steps = 6,
                colors = NordicSliderDefaults.colors()
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
                    .sizeIn(minWidth = 80.dp),
                text = when (publication) {
                    null -> "Unknown"
                    else -> "${countLog.roundToInt()}"
                },
                textAlign = TextAlign.End
            )
            Slider(
                enabled = countLog == 0f,
                value = period,
                onValueChange = { period = it },
                valueRange = RelayRetransmit.INTERVAL_RANGE.toFloat(),
                steps = 30,
                colors = NordicSliderDefaults.colors()
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
                    .sizeIn(minWidth = 80.dp),
                text = when (publication) {
                    null -> "Unknown"
                    else -> "${period.roundToInt()} ms"
                },
                textAlign = TextAlign.End
            )
        }
    )
}

/**
 * Returns the list of possible addresses that can be selected as a destination address for the
 * Heartbeat publication messages for a given ConfigurationServer Model.
 */
private fun Model.heartbeatPublicationDestinations(): List<HeartbeatPublicationDestination> {
    require(isConfigurationServer) { throw IllegalStateException("Model is not a Configuration Server") }
    val network = parentElement?.parentNode?.network
    val nodes = network?.nodes.orEmpty().map { it.primaryUnicastAddress }
    val groups = network?.groups.orEmpty().map { it.address as HeartbeatPublicationDestination }
    return nodes + groups + listOf<HeartbeatPublicationDestination>(
        AllRelays, AllFriends, AllProxies, AllNodes
    )
}