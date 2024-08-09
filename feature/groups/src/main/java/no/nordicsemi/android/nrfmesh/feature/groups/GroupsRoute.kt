package no.nordicsemi.android.nrfmesh.feature.groups

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GroupWork
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.kotlin.mesh.core.model.Group
import no.nordicsemi.kotlin.mesh.core.model.PrimaryGroupAddress
import java.util.UUID

@Composable
internal fun GroupsRoute(
    uiState: GroupsScreenUiState,
    navigateToGroup: (PrimaryGroupAddress) -> Unit,
    onSwiped: (PrimaryGroupAddress) -> Unit,
    onUndoClicked: (PrimaryGroupAddress) -> Unit,
    remove: (PrimaryGroupAddress) -> Unit
) {
    GroupsScreen(
        uiState = uiState,
        navigateToGroup = navigateToGroup,
        onSwiped = onSwiped,
        onUndoClicked = onUndoClicked,
        remove = remove
    )
}

@Composable
private fun GroupsScreen(
    uiState: GroupsScreenUiState,
    navigateToGroup: (PrimaryGroupAddress) -> Unit,
    onSwiped: (PrimaryGroupAddress) -> Unit,
    onUndoClicked: (PrimaryGroupAddress) -> Unit,
    remove: (PrimaryGroupAddress) -> Unit
) {

    when (uiState.groups.isEmpty()) {
        true -> MeshNoItemsAvailable(
            imageVector = Icons.Outlined.GroupWork,
            title = stringResource(R.string.no_groups_currently_added)
        )

        false -> {

        }
    }
}