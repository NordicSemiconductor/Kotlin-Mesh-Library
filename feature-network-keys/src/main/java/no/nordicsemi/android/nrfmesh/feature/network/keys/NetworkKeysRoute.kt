@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.feature.network.keys

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.ui.*
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.crypto.Utils.encodeHex

@Composable
internal fun NetworkKeysRoute(
    viewModel: NetworkKeysViewModel = hiltViewModel(),
    navigateToKey: (KeyIndex) -> Unit
) {
    val uiState: NetworkKeysScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    NetworkKeysScreen(
        uiState = uiState,
        navigateToKey = navigateToKey,
        onAddKeyClicked = viewModel::addNetworkKey,
        onSwiped = viewModel::onSwiped,
        onUndoClicked = viewModel::onUndoSwipe,
        remove = viewModel::remove
    )
}

@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
private fun NetworkKeysScreen(
    uiState: NetworkKeysScreenUiState,
    navigateToKey: (KeyIndex) -> Unit,
    onAddKeyClicked: () -> NetworkKey,
    onSwiped: (NetworkKey) -> Boolean,
    onUndoClicked: (NetworkKey) -> Unit,
    remove: (NetworkKey) -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = {
                navigateToKey(onAddKeyClicked().index)
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

            false -> NetworkKeys(
                context = context,
                coroutineScope = rememberCoroutineScope(),
                snackbarHostState = snackbarHostState,
                keys = uiState.keys,
                navigateToKey = navigateToKey,
                onSwiped = onSwiped,
                onUndoClicked = onUndoClicked,
                remove = remove
            )
        }
    }
}

@Composable
private fun NetworkKeys(
    context: Context,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    keys: List<NetworkKey>,
    navigateToKey: (KeyIndex) -> Unit,
    onSwiped: (NetworkKey) -> Boolean,
    onUndoClicked: (NetworkKey) -> Unit,
    remove: (NetworkKey) -> Unit
) {
    val listState = rememberLazyListState()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState
    ) {
        items(items = keys) { key ->
            SwipeToDismissKey(
                key = key,
                context = context,
                coroutineScope = coroutineScope,
                snackbarHostState = snackbarHostState,
                navigateToNetworkKey = navigateToKey,
                onSwiped = onSwiped,
                onUndoClicked = onUndoClicked,
                remove = remove
            )
        }
    }
}

@Composable
private fun SwipeToDismissKey(
    key: NetworkKey,
    context: Context,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    navigateToNetworkKey: (KeyIndex) -> Unit,
    onSwiped: (NetworkKey) -> Boolean,
    onUndoClicked: (NetworkKey) -> Unit,
    remove: (NetworkKey) -> Unit
) {
    // Hold the current state from the Swipe to Dismiss composable
    val dismissState = rememberDismissState()
    SwipeDismissItem(
        dismissState = dismissState,
        content = {
            Surface(color = MaterialTheme.colorScheme.background) {
                MeshTwoLineListItem(
                    modifier = Modifier.clickable {
                        navigateToNetworkKey(key.index)
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
    if (dismissState.currentValue != DismissValue.Default) {
        if (onSwiped(key)) {
            showSnackbar(
                scope = coroutineScope,
                snackbarHostState = snackbarHostState,
                message = stringResource(R.string.label_network_key_deleted),
                actionLabel = stringResource(R.string.action_undo),
                onDismissed = { remove(key) },
                onActionPerformed = {
                    coroutineScope.launch {
                        dismissState.reset()
                        onUndoClicked(key)
                    }
                },
                withDismissAction = true
            )
        } else {
            LaunchedEffect(key1 = dismissState) {
                dismissState.reset()
                showSnackbar(
                    scope = coroutineScope,
                    snackbarHostState = snackbarHostState,
                    message = context.getString(R.string.error_cannot_delete_key_in_use),
                    withDismissAction = true
                )
            }
        }
    }
}