package no.nordicsemi.android.nrfmesh.feature.model.configurationserver

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SportsScore
import androidx.compose.material.icons.outlined.Start
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
import no.nordicsemi.android.nrfmesh.core.ui.MeshTwoLineListItem
import no.nordicsemi.android.nrfmesh.feature.models.R
import no.nordicsemi.kotlin.mesh.core.model.HeartbeatSubscriptionSource
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExposedDropdownMenuBoxScope.HeartbeatSubscriptionSourcesDropdownMenu(
    network: MeshNetwork?,
    model: Model,
    expanded: Boolean,
    onDismissed: () -> Unit,
    onSourceSelected: (HeartbeatSubscriptionSource) -> Unit,
) {
    val node = model.parentElement?.parentNode ?: return
    val otherNodes = network?.nodes?.filter { it != node }.orEmpty()
    DropdownMenu(
        modifier = Modifier
            .exposedDropdownSize()
            .wrapContentHeight(),
        expanded = expanded,
        onDismissRequest = onDismissed,
        content = {
            Text(
                modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                text = stringResource(R.string.label_unicast_sources)
            )
            otherNodes.forEachIndexed { index, otherNode ->
                DropdownMenuItem(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    text = {
                        MeshTwoLineListItem(
                            imageVector = Icons.Outlined.Start,
                            title = otherNode.name,
                            subtitle = otherNode.primaryUnicastAddress.address.toHexString(
                                format = HexFormat {
                                    number.prefix = "0x"
                                    upperCase = true
                                }
                            )
                        )
                    },
                    onClick = { onSourceSelected(otherNode.primaryUnicastAddress) }
                )
                if (index < otherNodes.size) {
                    HorizontalDivider()
                }
            }
        }
    )
}