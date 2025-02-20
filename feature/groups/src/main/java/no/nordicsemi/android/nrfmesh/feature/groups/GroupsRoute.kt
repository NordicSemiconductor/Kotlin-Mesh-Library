package no.nordicsemi.android.nrfmesh.feature.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import no.nordicsemi.kotlin.mesh.core.model.PrimaryGroupAddress

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun GroupsRoute(
    uiState: GroupsScreenUiState,
    navigateToGroup: (PrimaryGroupAddress) -> Unit,
) {
    FlowRow(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 8.dp)
            .verticalScroll(state = rememberScrollState()),
        maxItemsInEachRow = 5,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        uiState.groups.forEach { group ->
            MeshItem(
                icon = {
                    Icon(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                            .padding(2.dp),
                        imageVector = Icons.Outlined.GroupWork,
                        tint = Color.White,
                        contentDescription = null
                    )
                },
                title = group.name,
                subtitle = "0x${group.address.toHexString()}",
                onClick = { navigateToGroup(group.address) },
            )
        }
    }
    if (uiState.groups.isEmpty()) MeshNoItemsAvailable(
        imageVector = Icons.Outlined.GroupWork,
        title = stringResource(R.string.label_no_groups_currently_added),
        rationale = stringResource(R.string.label_no_groups_currently_added_rationale),
    )
}