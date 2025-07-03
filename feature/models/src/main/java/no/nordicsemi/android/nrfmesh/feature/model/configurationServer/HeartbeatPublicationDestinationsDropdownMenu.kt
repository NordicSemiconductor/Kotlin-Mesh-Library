package no.nordicsemi.android.nrfmesh.feature.model.configurationServer

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
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
import no.nordicsemi.android.nrfmesh.core.common.name
import no.nordicsemi.android.nrfmesh.core.ui.MeshSingleLineListItem
import no.nordicsemi.android.nrfmesh.feature.models.R
import no.nordicsemi.kotlin.mesh.core.model.HeartbeatPublicationDestination
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.fixedGroupAddresses

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExposedDropdownMenuBoxScope.HeartbeatPublicationDestinationsDropdownMenu(
    network: MeshNetwork?,
    expanded: Boolean,
    onDismissed: () -> Unit,
    onDestinationSelected: (HeartbeatPublicationDestination) -> Unit,
    onAddGroupClicked: () -> Unit,
) {
    val elements = network?.nodes.orEmpty().flatMap { it.elements }
    val groups = network?.groups.orEmpty().map { it.address as HeartbeatPublicationDestination }
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
                    onClick = {
                        onDestinationSelected(element.unicastAddress)
                    }
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
                                    imageVector = Icons.Outlined.SportsScore,
                                    contentDescription = null
                                )
                            },
                            title = network
                                ?.group(address = destination.address)?.name
                                ?: destination.toHexString(),
                        )
                    },
                    onClick = { onDestinationSelected(destination) }
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
                                    imageVector = Icons.Outlined.SportsScore,
                                    contentDescription = null
                                )
                            },
                            title = destination.name(),
                        )
                    },
                    onClick = { onDestinationSelected(destination) }
                )
            }
        }
    )
}