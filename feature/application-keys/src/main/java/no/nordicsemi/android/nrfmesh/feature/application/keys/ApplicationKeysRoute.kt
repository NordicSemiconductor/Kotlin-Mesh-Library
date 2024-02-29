@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.feature.application.keys

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.feature.application.keys.R
import no.nordicsemi.android.nrfmesh.core.ui.*
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.crypto.Utils.encodeHex

@Composable
internal fun ApplicationKeysRoute(
    uiState: ApplicationKeysScreenUiState,
    navigateToApplicationKey: (KeyIndex) -> Unit,
    onAddKeyClicked: () -> ApplicationKey,
    onSwiped: (ApplicationKey) -> Unit,
    onUndoClicked: (ApplicationKey) -> Unit,
    remove: (ApplicationKey) -> Unit
) {
    val context = LocalContext.current
    ApplicationsKeysScreen(
        context = context,
        uiState = uiState,
        navigateToApplicationKey = navigateToApplicationKey,
        onAddKeyClicked = onAddKeyClicked,
        onSwiped = onSwiped,
        onUndoClicked = onUndoClicked,
        remove = remove
    )
}

@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
private fun ApplicationsKeysScreen(
    context: Context,
    uiState: ApplicationKeysScreenUiState,
    navigateToApplicationKey: (KeyIndex) -> Unit,
    onAddKeyClicked: () -> ApplicationKey,
    onSwiped: (ApplicationKey) -> Unit,
    onUndoClicked: (ApplicationKey) -> Unit,
    remove: (ApplicationKey) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = {
                // Dismiss any snack bars that are being currently displayed.
                dismissSnackbar(snackbarHostState = snackbarHostState)
                navigateToApplicationKey(onAddKeyClicked().index)
            }) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(R.string.action_add_key)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        when (uiState.keys.isEmpty()) {
            true -> MeshNoItemsAvailable(
                imageVector = Icons.Outlined.VpnKey,
                title = stringResource(R.string.label_no_keys_added)
            )

            false -> ApplicationKeys(
                context = context,
                listState = listState,
                snackbarHostState = snackbarHostState,
                keys = uiState.keys,
                navigateToApplicationKey = navigateToApplicationKey,
                onSwiped = onSwiped,
                onUndoClicked = onUndoClicked,
                remove = remove
            )
        }
    }
}

@Composable
private fun ApplicationKeys(
    context: Context,
    listState: LazyListState,
    snackbarHostState: SnackbarHostState,
    keys: List<ApplicationKey>,
    navigateToApplicationKey: (KeyIndex) -> Unit,
    onSwiped: (ApplicationKey) -> Unit,
    onUndoClicked: (ApplicationKey) -> Unit,
    remove: (ApplicationKey) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        items(items = keys, key = { it.key.hashCode() }) { key ->
            SwipeToDismissKey(
                key = key,
                context = context,
                snackbarHostState = snackbarHostState,
                navigateToApplicationKey = navigateToApplicationKey,
                onSwiped = onSwiped,
                onUndoClicked = onUndoClicked,
                remove = remove
            )
        }
    }
}

@Composable
private fun SwipeToDismissKey(
    key: ApplicationKey,
    context: Context,
    snackbarHostState: SnackbarHostState,
    navigateToApplicationKey: (KeyIndex) -> Unit,
    onSwiped: (ApplicationKey) -> Unit,
    onUndoClicked: (ApplicationKey) -> Unit,
    remove: (ApplicationKey) -> Unit
) {
    // Hold the current state from the Swipe to Dismiss composable
    var shouldNotDismiss by remember { mutableStateOf(true) }
    val dismissState = rememberSwipeToDismissState(
        confirmValueChange = {
            shouldNotDismiss = !key.isInUse
            shouldNotDismiss
        },
        positionalThreshold = { it * 0.5f }
    )
    SwipeDismissItem(
        dismissState = dismissState,
        content = {
            ElevatedCardItem(
                modifier = Modifier
                    .clickable { navigateToApplicationKey(key.index) },
                imageVector = Icons.Outlined.VpnKey,
                title = key.name,
                subtitle = key.key.encodeHex()
            )
        }
    )

    if (!shouldNotDismiss) {
        LaunchedEffect(snackbarHostState) {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.error_cannot_delete_key_in_use),
                withDismissAction = true,
                duration = SnackbarDuration.Short,
            )
        }
    }
    if (dismissState.isDismissed()) {
        LaunchedEffect(snackbarHostState) {
            onSwiped(key)
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.label_application_key_deleted),
                actionLabel = context.getString(R.string.action_undo),
                withDismissAction = true,
                duration = SnackbarDuration.Long,
            ).also {
                when (it) {
                    SnackbarResult.Dismissed -> remove(key)
                    SnackbarResult.ActionPerformed -> {
                        dismissState.reset()
                        onUndoClicked(key)
                    }
                }
            }
        }
    }
}