package no.nordicsemi.android.nrfmesh.feature.groups

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GroupWork
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.kotlin.mesh.core.model.Node
import java.util.UUID

@Composable
internal fun GroupsRoute(viewModel: GroupsViewModel) {
    val uiState: GroupsScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    GroupsScreen(
        uiState = uiState,
        navigateToNode = { },
        onSwiped = { },
        onUndoClicked = { },
        remove = { }
    )
}

@Composable
private fun GroupsScreen(
    uiState: GroupsScreenUiState,
    navigateToNode: (UUID) -> Unit,
    onSwiped: (Node) -> Unit,
    onUndoClicked: (Node) -> Unit,
    remove: (Node) -> Unit
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