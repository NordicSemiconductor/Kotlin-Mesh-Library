@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLifecycleComposeApi::class,
    ExperimentalMaterialApi::class
)

package no.nordicsemi.android.nrfmesh.feature.network.keys

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
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
    navigateToNetworkKey: (KeyIndex) -> Unit,
    onBackClicked: () -> Unit
) {
    val uiState: NetworkKeysScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    NetworkKeysScreen(
        uiState = uiState,
        navigateToNetworkKey = navigateToNetworkKey,
        onAddKeyClicked = viewModel::addNetworkKey,
        onSwiped = viewModel::onSwiped,
        onUndoClicked = viewModel::onUndoSwipe,
        remove = viewModel::remove
    ) {
        viewModel.removeKeys()
        onBackClicked()
    }
}

@Composable
private fun NetworkKeysScreen(
    uiState: NetworkKeysScreenUiState,
    navigateToNetworkKey: (KeyIndex) -> Unit,
    onAddKeyClicked: () -> NetworkKey,
    onSwiped: (NetworkKey) -> Unit,
    onUndoClicked: (NetworkKey) -> Unit,
    remove: (NetworkKey) -> Unit,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(connection = scrollBehavior.nestedScrollConnection),
        topBar = {
            MeshLargeTopAppBar(
                title = stringResource(id = R.string.label_network_keys),
                navigationIcon = {
                    IconButton(onClick = {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        onBackPressed()
                    }) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = {
                navigateToNetworkKey(onAddKeyClicked().index)
            }) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(R.string.action_add_key)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (uiState.keys.isEmpty()) {
            true -> MeshNoItemsAvailable(
                imageVector = Icons.Outlined.VpnKey,
                title = stringResource(R.string.label_no_keys_added)
            )
            false -> NetworkKeys(
                    padding = padding,
                    context = context,
                    coroutineScope = rememberCoroutineScope(),
                    snackbarHostState = snackbarHostState,
                    keys = uiState.keys,
                    navigateToApplicationKey = navigateToNetworkKey,
                    onSwiped = onSwiped,
                    onUndoClicked = onUndoClicked,
                    remove = remove
                )
        }
    }
}

@Composable
private fun NetworkKeys(
    padding: PaddingValues,
    context: Context,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    keys: List<NetworkKey>,
    navigateToApplicationKey: (KeyIndex /* = kotlin.UShort */) -> Unit,
    onSwiped: (NetworkKey) -> Unit,
    onUndoClicked: (NetworkKey) -> Unit,
    remove: (NetworkKey) -> Unit
) {
    val listState = rememberLazyListState()
    LazyColumn(
        contentPadding = padding,
        modifier = Modifier.fillMaxSize(),
        state = listState
    ) {
        items(items = keys) { key ->
            // Hold the current state from the Swipe to Dismiss composable
            val dismissState = rememberDismissState {
                val state = it == DismissValue.DismissedToStart && key.isInUse()
                if (state) {
                    showSnackbar(
                        scope = coroutineScope,
                        snackbarHostState = snackbarHostState,
                        message = context.getString(R.string.error_cannot_delete_key_in_use),
                        withDismissAction = true
                    )
                }
                !state
            }
            var keyDismissed by remember { mutableStateOf(false) }
            if (keyDismissed) {
                showSnackbar(
                    scope = coroutineScope,
                    snackbarHostState = snackbarHostState,
                    message = stringResource(R.string.label_network_key_deleted),
                    actionLabel = stringResource(R.string.action_undo),
                    onDismissed = { remove(key) },
                    onActionPerformed = {
                        onUndoClicked(key)
                        coroutineScope.launch {
                            dismissState.reset()
                        }
                    },
                    withDismissAction = true
                )
            }
            SwipeDismissItem(
                dismissState = dismissState,
                background = { offsetX ->
                    val color = if (offsetX < (-30).dp) Color.Red else Color.DarkGray
                    val scale by animateFloatAsState(if (offsetX < (-50).dp) 1f else 0.75f)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color)
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            modifier = Modifier.scale(scale),
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "null"
                        )
                    }
                },
                content = { isDismissed ->
                    if (isDismissed) {
                        onSwiped(key)
                    }
                    keyDismissed = isDismissed
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
        }
    }
}