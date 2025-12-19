package no.nordicsemi.android.nrfmesh.feature.model.configurationserver

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.nrfmesh.core.common.name
import no.nordicsemi.android.nrfmesh.core.ui.MeshSingleLineListItem
import no.nordicsemi.android.nrfmesh.core.ui.MeshTwoLineListItem
import no.nordicsemi.android.nrfmesh.feature.models.R
import no.nordicsemi.kotlin.mesh.core.model.HeartbeatPublicationDestination
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.VirtualAddress
import no.nordicsemi.kotlin.mesh.core.model.fixedGroupAddresses

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExposedDropdownMenuBoxScope.HeartbeatPublicationDestinationsDropdownMenu(
    network: MeshNetwork?,
    model: Model,
    expanded: Boolean,
    onDismissed: () -> Unit,
    onDestinationSelected: (HeartbeatPublicationDestination) -> Unit,
    onAddGroupClicked: () -> Unit,
) {
    val node = model.parentElement?.parentNode ?: return
    val otherNodes = network?.nodes?.filter { it != node }.orEmpty()
    val groups = network?.groups?.filter { it.address !is VirtualAddress }.orEmpty()
    DropdownMenu(
        modifier = Modifier
            .exposedDropdownSize()
            .wrapContentHeight(),
        expanded = expanded,
        onDismissRequest = onDismissed,
        content = {
            Text(
                modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                text = stringResource(R.string.label_unicast_destinations)
            )
            otherNodes.forEach { otherNode ->
                DropdownMenuItem(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    text = {
                        MeshSingleLineListItem(
                            imageVector = Icons.Outlined.SportsScore,
                            title = otherNode.name,
                        )
                    },
                    onClick = { onDestinationSelected(otherNode.primaryUnicastAddress) }
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
                            imageVector = Icons.Outlined.SportsScore,
                            title = network
                                ?.group(address = destination.address.address)?.name
                                ?: destination.address.address.toHexString(
                                    format = HexFormat {
                                        number.prefix = "0x"
                                        upperCase = true
                                    }
                                ),
                        )
                    },
                    onClick = { onDestinationSelected(destination.address as HeartbeatPublicationDestination) }
                )
            }
            DropdownMenuItem(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                text = {
                    MeshTwoLineListItem(
                        imageVector = Icons.Outlined.Add,
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
                            imageVector = Icons.Outlined.SportsScore,
                            title = destination.name(),
                        )
                    },
                    onClick = { onDestinationSelected(destination) }
                )
            }
        }
    )
}