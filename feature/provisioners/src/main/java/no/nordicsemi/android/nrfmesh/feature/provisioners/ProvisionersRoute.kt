@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.feature.provisioners

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.models.ProvisionerData
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.core.ui.SwipeDismissItem
import no.nordicsemi.android.nrfmesh.core.ui.isDismissed
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import java.util.Locale
import java.util.UUID

@Composable
internal fun ProvisionersRoute(
    highlightSelectedItem: Boolean,
    provisioners: List<ProvisionerData>,
    onAddProvisionerClicked: () -> Provisioner,
    onSwiped: (ProvisionerData) -> Unit,
    onUndoClicked: (ProvisionerData) -> Unit,
    remove: (ProvisionerData) -> Unit,
    navigateToProvisioner: (UUID) -> Unit
) {
    Provisioners(
        highlightSelectedItem = highlightSelectedItem,
        provisioners = provisioners,
        addProvisioner = onAddProvisionerClicked,
        onSwiped = onSwiped,
        onUndoClicked = onUndoClicked,
        remove = remove,
        navigateToProvisioner = navigateToProvisioner
    )
}

@Composable
private fun Provisioners(
    highlightSelectedItem: Boolean,
    provisioners: List<ProvisionerData>,
    addProvisioner: () -> Provisioner,
    onSwiped: (ProvisionerData) -> Unit,
    onUndoClicked: (ProvisionerData) -> Unit,
    remove: (ProvisionerData) -> Unit,
    navigateToProvisioner: (UUID) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedUuid by remember { mutableStateOf<UUID?>(null) }
    Scaffold(
        modifier = Modifier.background(color = Color.Red),
        contentWindowInsets = WindowInsets(top = 8.dp),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier.defaultMinSize(minWidth = 150.dp),
                text = { Text(text = stringResource(R.string.label_add_provisioner)) },
                icon = { Icon(imageVector = Icons.Outlined.Add, contentDescription = null) },
                onClick = {
                    runCatching {
                        addProvisioner()
                    }.onSuccess {
                        selectedUuid = it.uuid
                        navigateToProvisioner(it.uuid)
                    }
                },
                expanded = true
            )
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            item { SectionTitle(title = stringResource(id = R.string.label_provisioners)) }
            items(items = provisioners, key = { it.uuid }) { provisioner ->
                SwipeToDismissProvisioner(
                    provisioner = provisioner,
                    scope = scope,
                    context = context,
                    snackbarHostState = snackbarHostState,
                    isSelected = selectedUuid == provisioner.uuid && highlightSelectedItem,
                    navigateToProvisioner = {
                        selectedUuid = it
                        navigateToProvisioner(it)
                    },
                    onSwiped = onSwiped,
                    onUndoClicked = onUndoClicked,
                    remove = remove,
                    isOnlyProvisioner = { provisioners.size == 1 }
                )
            }
        }
    }
}

@Composable
private fun SwipeToDismissProvisioner(
    scope: CoroutineScope,
    context: Context,
    snackbarHostState: SnackbarHostState,
    provisioner: ProvisionerData,
    navigateToProvisioner: (UUID) -> Unit,
    onSwiped: (ProvisionerData) -> Unit,
    onUndoClicked: (ProvisionerData) -> Unit,
    remove: (ProvisionerData) -> Unit,
    isSelected: Boolean = false,
    isOnlyProvisioner: () -> Boolean
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            handleValueChange(
                scope = scope,
                context = context,
                snackbarHostState = snackbarHostState,
                isOnlyProvisioner = isOnlyProvisioner
            )
        },
        positionalThreshold = { it * 0.5f }
    )
    SwipeDismissItem(
        dismissState = dismissState,
        content = {
            ElevatedCardItem(
                colors = when (isSelected) {
                    true -> CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    else -> CardDefaults.outlinedCardColors()
                },
                onClick = { navigateToProvisioner(provisioner.uuid) },
                imageVector = Icons.Outlined.PersonOutline,
                title = provisioner.name,
                subtitle = provisioner.uuid.toString().uppercase(Locale.US)
            )
        }
    )
    if (dismissState.isDismissed()) {
        LaunchedEffect(snackbarHostState) {
            scope.launch {
                delay(250)
                onSwiped(provisioner)
            }
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.label_provisioner_deleted),
                    actionLabel = context.getString(R.string.action_undo),
                    withDismissAction = true,
                    duration = SnackbarDuration.Short,
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
}

private fun handleValueChange(
    scope: CoroutineScope,
    context: Context,
    snackbarHostState: SnackbarHostState,
    isOnlyProvisioner: () -> Boolean
): Boolean = when {

    isOnlyProvisioner() -> {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.error_cannot_delete_last_provisioner)
            )
        }
        false
    }

    else -> true
}