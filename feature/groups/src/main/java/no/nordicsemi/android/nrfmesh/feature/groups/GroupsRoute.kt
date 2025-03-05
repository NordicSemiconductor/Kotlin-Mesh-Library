package no.nordicsemi.android.nrfmesh.feature.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GroupWork
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.nrfmesh.core.ui.MeshItem
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.android.nrfmesh.core.ui.isCompactWidth
import no.nordicsemi.kotlin.mesh.core.model.GroupAddress
import no.nordicsemi.kotlin.mesh.core.model.PrimaryGroupAddress
import no.nordicsemi.kotlin.mesh.core.model.VirtualAddress

@OptIn(ExperimentalLayoutApi::class, ExperimentalStdlibApi::class)
@Composable
internal fun GroupsRoute(
    uiState: GroupsScreenUiState,
    navigateToGroup: (PrimaryGroupAddress) -> Unit,
) {
    if (isCompactWidth()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(all = 16.dp),
            content = {
                items(
                    items = uiState.groups,
                    key = { it.address.toHexString() }
                ) { group ->
                    MeshItem(
                        modifier = Modifier.fillMaxWidth(),
                        icon = {
                            Icon(
                                modifier = Modifier
                                    .size(size = 24.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    )
                                    .padding(all = 2.dp),
                                imageVector = Icons.Outlined.GroupWork,
                                tint = Color.White,
                                contentDescription = null
                            )
                        },
                        title = group.name,
                        subtitle = group.address.run {
                            if (this is GroupAddress) {
                                "0x${group.address.toHexString()}"
                            } else {
                                (this as VirtualAddress).uuid.toString().uppercase()
                            }
                        },
                        onClick = { navigateToGroup(group.address) },
                    )
                }
            }
        )
    } else {
        FlowRow(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(state = rememberScrollState()),
            maxItemsInEachRow = 5,
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
            verticalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            Spacer(
                modifier = Modifier
                    .height(height = 8.dp)
                    .fillMaxWidth()
            )
            uiState.groups.forEach { group ->
                MeshItem(
                    icon = {
                        Icon(
                            modifier = Modifier
                                .size(size = 24.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .padding(all = 2.dp),
                            imageVector = Icons.Outlined.GroupWork,
                            tint = Color.White,
                            contentDescription = null
                        )
                    },
                    title = group.name,
                    subtitle = group.address.run {
                        if (this is GroupAddress) {
                            "0x${group.address.address.toHexString(format = HexFormat.UpperCase)}"
                        } else {
                            (this as VirtualAddress).uuid.toString().uppercase()
                        }
                    },
                    onClick = { navigateToGroup(group.address) },
                )
            }
            Spacer(
                modifier = Modifier
                    .height(height = 8.dp)
                    .fillMaxWidth()
            )
        }
    }
    if (uiState.groups.isEmpty()) MeshNoItemsAvailable(
        imageVector = Icons.Outlined.GroupWork,
        title = stringResource(R.string.label_no_groups_currently_added),
        rationale = stringResource(R.string.label_no_groups_currently_added_rationale),
    )
}