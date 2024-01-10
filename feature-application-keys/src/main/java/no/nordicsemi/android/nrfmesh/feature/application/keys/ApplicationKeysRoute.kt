@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.feature.application.keys

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.clickable
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.android.feature.application.keys.R
import no.nordicsemi.android.nrfmesh.core.ui.*
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.crypto.Utils.encodeHex

@Composable
internal fun ApplicationKeysRoute(
    viewModel: ApplicationKeysViewModel = hiltViewModel(),
    navigateToApplicationKey: (KeyIndex) -> Unit
) {
    val uiState: ApplicationKeysScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    ApplicationsKeysScreen(
        uiState = uiState,
        navigateToApplicationKey = navigateToApplicationKey,
        onAddKeyClicked = viewModel::addApplicationKey,
        onSwiped = viewModel::onSwiped,
        onUndoClicked = viewModel::onUndoSwipe,
        remove = viewModel::remove
    )
}

@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
private fun ApplicationsKeysScreen(
    uiState: ApplicationKeysScreenUiState,
    navigateToApplicationKey: (KeyIndex) -> Unit,
    onAddKeyClicked: () -> ApplicationKey,
    onSwiped: (ApplicationKey) -> Unit,
    onUndoClicked: (ApplicationKey) -> Unit,
    remove: (ApplicationKey) -> Unit
) {
    val context = LocalContext.current
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
                coroutineScope = rememberCoroutineScope(),
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
    coroutineScope: CoroutineScope,
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
        state = listState
    ) {
        items(items = keys, key = { it.key.hashCode() }) { key ->
            SwipeToDismissKey(
                key = key,
                context = context,
                coroutineScope = coroutineScope,
                snackbarHostState = snackbarHostState,
                navigateToApplicationKey = navigateToApplicationKey,
                onSwiped = onSwiped,
                onUndoClicked = onUndoClicked,
                remove = remove
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissKey(
    key: ApplicationKey,
    context: Context,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    navigateToApplicationKey: (KeyIndex) -> Unit,
    onSwiped: (ApplicationKey) -> Unit,
    onUndoClicked: (ApplicationKey) -> Unit,
    remove: (ApplicationKey) -> Unit
) {
    // Hold the current state from the Swipe to Dismiss composable
    var shouldNotDismiss by remember {
        mutableStateOf(false)
    }
    val dismissState = rememberSwipeToDismissState(
        confirmValueChange = {
            shouldNotDismiss = key.isInUse
            !shouldNotDismiss
        }
    )
    SwipeDismissItem(
        dismissState = dismissState,
        content = {
            Surface(color = MaterialTheme.colorScheme.background) {
                MeshTwoLineListItem(
                    modifier = Modifier.clickable {
                        navigateToApplicationKey(key.index)
                    },
                    leadingComposable = {
                        Icon(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            imageVector = Icons.Outlined.VpnKey,
                            contentDescription = null,
                            tint = LocalContentColor.current.copy(alpha = 0.6f)
                        )
                    },
                    title = key.name,
                    subtitle = key.key.encodeHex()
                )
            }
        }
    )

    if (shouldNotDismiss) {
        LaunchedEffect(snackbarHostState) {
            showSnackbar(
                scope = coroutineScope,
                snackbarHostState = snackbarHostState,
                message = context.getString(R.string.error_cannot_delete_key_in_use),
                duration = SnackbarDuration.Short,
                onDismissed = {
                    shouldNotDismiss = false
                }
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