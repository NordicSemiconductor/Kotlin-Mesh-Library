package no.nordicsemi.android.nrfmesh.feature.model.configurationServer

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.GroupWork
import androidx.compose.material.icons.outlined.SportsScore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.nrfmesh.core.ui.MeshSingleLineListItem
import no.nordicsemi.android.nrfmesh.feature.configurationserver.R
import no.nordicsemi.kotlin.mesh.core.model.AllFriends
import no.nordicsemi.kotlin.mesh.core.model.AllNodes
import no.nordicsemi.kotlin.mesh.core.model.AllProxies
import no.nordicsemi.kotlin.mesh.core.model.AllRelays
import no.nordicsemi.kotlin.mesh.core.model.HeartbeatPublicationDestination
import no.nordicsemi.kotlin.mesh.core.model.HeartbeatSubscriptionDestination
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExposedDropdownMenuBoxScope.HeartbeatSubscriptionDestinationsDropdownMenu(
    network: MeshNetwork?,
    expanded: Boolean,
    onDismissed: () -> Unit,
    onDestinationSelected: (HeartbeatSubscriptionDestination) -> Unit,
    onAddGroupClicked: () -> Unit
) {
    val elements = network?.nodes.orEmpty().flatMap { it.elements }
    val groups = network?.groups.orEmpty().map { it.address as HeartbeatPublicationDestination }
    val fixedGroupAddresses = listOf(AllRelays, AllFriends, AllProxies, AllNodes)
    DropdownMenu(
        modifier = Modifier
            .exposedDropdownSize()
            .wrapContentHeight(),
        expanded = expanded,
        onDismissRequest = onDismissed,
        content = {
            Text(
                modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                text = stringResource(R.string.label_elements)
            )
            elements.forEach { element ->
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
                            title = element.name?.let {
                                "$it: 0x${element.unicastAddress.toHexString()}"
                            } ?: element.unicastAddress.toHexString()
                        )
                    },
                    onClick = { onDestinationSelected(element.unicastAddress) }
                )
            }
            HorizontalDivider()
            Text(
                modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                text = stringResource(R.string.label_groups)
            )
            groups.forEach { destination ->
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
                                    imageVector = Icons.Outlined.GroupWork,
                                    contentDescription = null
                                )
                            },
                            title = network
                                ?.group(address = destination.address)?.name
                                ?: destination.toHexString(),
                        )
                    },
                    onClick = { onDestinationSelected(destination as HeartbeatSubscriptionDestination) }
                )
            }
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
                                imageVector = Icons.Outlined.Add,
                                contentDescription = null
                            )
                        },
                        title = stringResource(R.string.add_group)
                    )
                },
                onClick = onAddGroupClicked
            )
            HorizontalDivider()
            Text(
                modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                text = stringResource(R.string.label_fixed_group_addresses)
            )
            fixedGroupAddresses.forEach { destination ->
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
                                    imageVector = Icons.Outlined.GroupWork,
                                    contentDescription = null
                                )
                            },
                            title = when (destination) {
                                is AllRelays -> stringResource(R.string.label_all_relays)
                                is AllFriends -> stringResource(R.string.label_all_friends)
                                is AllProxies -> stringResource(R.string.label_all_proxies)
                                is AllNodes -> stringResource(R.string.label_all_nodes)
                            },
                        )
                    },
                    onClick = {
                        onDestinationSelected(destination)
                    }
                )
            }
        }
    )
}