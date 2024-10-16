@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.feature.provisioners

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.android.nrfmesh.core.ui.SwipeDismissItem
import no.nordicsemi.android.nrfmesh.core.ui.isDismissed
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.ProvisionersScreen
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import java.util.Locale
import java.util.UUID

@Composable
internal fun ProvisionersRoute(
    appState: AppState,
    uiState: ProvisionersScreenUiState,
    navigateToProvisioner: (UUID) -> Unit,
    onAddProvisionerClicked: () -> Provisioner,
    onSwiped: (Provisioner) -> Unit,
    onUndoClicked: (Provisioner) -> Unit,
    remove: (Provisioner) -> Unit,
    onBackPressed: () -> Unit
) {
    val screen = appState.currentScreen as? ProvisionersScreen
    LaunchedEffect(key1 = screen) {
        screen?.buttons?.onEach { button ->
            when (button) {
                ProvisionersScreen.Actions.ADD_PROVISIONER ->  {
                    navigateToProvisioner(onAddProvisionerClicked().uuid)
                }
                ProvisionersScreen.Actions.BACK -> onBackPressed()
            }

        }?.launchIn(this)
    }
    ProvisionersScreen(
        uiState = uiState,
        navigateToProvisioner = navigateToProvisioner,
        onSwiped = onSwiped,
        onUndoClicked = onUndoClicked,
        remove = remove
    )
}

@Composable
private fun ProvisionersScreen(
    uiState: ProvisionersScreenUiState,
    navigateToProvisioner: (UUID) -> Unit,
    onSwiped: (Provisioner) -> Unit,
    onUndoClicked: (Provisioner) -> Unit,
    remove: (Provisioner) -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    when (uiState.provisioners.isEmpty()) {
        true -> MeshNoItemsAvailable(
            imageVector = Icons.Outlined.AutoAwesome,
            title = stringResource(R.string.no_provisioners_currently_added)
        )

        false -> Provisioners(
            context = context,
            snackbarHostState = snackbarHostState,
            provisioners = uiState.provisioners,
            navigateToProvisioner = navigateToProvisioner,
            onSwiped = onSwiped,
            onUndoClicked = onUndoClicked,
            remove = remove
        )
    }
}

@Composable
private fun Provisioners(
    context: Context,
    snackbarHostState: SnackbarHostState,
    provisioners: List<Provisioner>,
    navigateToProvisioner: (UUID) -> Unit,
    onSwiped: (Provisioner) -> Unit,
    onUndoClicked: (Provisioner) -> Unit,
    remove: (Provisioner) -> Unit
) {
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        items(items = provisioners, key = { it.uuid }) { provisioner ->
            SwipeToDismissProvisioner(
                provisioner = provisioner,
                context = context,
                snackbarHostState = snackbarHostState,
                navigateToProvisioner = navigateToProvisioner,
                onSwiped = onSwiped,
                onUndoClicked = onUndoClicked,
                remove = remove
            )
        }
    }
}

@Composable
private fun SwipeToDismissProvisioner(
    provisioner: Provisioner,
    context: Context,
    snackbarHostState: SnackbarHostState,
    navigateToProvisioner: (UUID) -> Unit,
    onSwiped: (Provisioner) -> Unit,
    onUndoClicked: (Provisioner) -> Unit,
    remove: (Provisioner) -> Unit
) {
    // Hold the current state from the Swipe to Dismiss composable
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { it * 0.5f }
    )
    SwipeDismissItem(
        dismissState = dismissState,
        content = {
            ElevatedCardItem(
                onClick = { navigateToProvisioner(provisioner.uuid) },
                imageVector = Icons.Outlined.VpnKey,
                title = provisioner.name,
                subtitle = provisioner.uuid.toString().uppercase(Locale.US)
            )
        }
    )
    if (dismissState.isDismissed()) {
        LaunchedEffect(snackbarHostState) {
            onSwiped(provisioner)
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.label_provisioner_deleted),
                actionLabel = context.getString(R.string.action_undo),
                withDismissAction = true,
                duration = SnackbarDuration.Long,
            ).also {
                when (it) {
                    SnackbarResult.Dismissed -> remove(provisioner)
                    SnackbarResult.ActionPerformed -> {
                        dismissState.reset()
                        onUndoClicked(provisioner)
                    }
                }
            }
        }
    }
}