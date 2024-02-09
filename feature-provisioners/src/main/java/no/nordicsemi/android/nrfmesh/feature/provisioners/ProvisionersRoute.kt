@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.feature.provisioners

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.feature.provisioners.R
import no.nordicsemi.android.nrfmesh.core.ui.*
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import java.util.*

@Composable
internal fun ProvisionersRoute(
    viewModel: ProvisionersViewModel = hiltViewModel(),
    navigateToProvisioner: (UUID) -> Unit
) {
    val uiState: ProvisionersScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProvisionersScreen(
        uiState = uiState,
        navigateToProvisioner = navigateToProvisioner,
        onAddProvisionerClicked = viewModel::addProvisioner,
        onSwiped = viewModel::onSwiped,
        onUndoClicked = viewModel::onUndoSwipe,
        remove = viewModel::remove
    )
}

@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
private fun ProvisionersScreen(
    uiState: ProvisionersScreenUiState,
    navigateToProvisioner: (UUID) -> Unit,
    onAddProvisionerClicked: () -> Provisioner,
    onSwiped: (Provisioner) -> Unit,
    onUndoClicked: (Provisioner) -> Unit,
    remove: (Provisioner) -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    navigateToProvisioner(onAddProvisionerClicked().uuid)
                }
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(R.string.action_add_provisioner)
                )
            }
        }
    ) {
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
    val dismissState = rememberSwipeToDismissState(
        positionalThreshold = { it * 0.5f }
    )
    SwipeDismissItem(
        dismissState = dismissState,
        content = {
            ElevatedCardItem(
                modifier = Modifier
                    .clickable { navigateToProvisioner(provisioner.uuid) },
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