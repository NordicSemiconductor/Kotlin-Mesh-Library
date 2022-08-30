@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class,
    ExperimentalLifecycleComposeApi::class
)

package no.nordicsemi.android.feature.application.keys

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.ui.MeshLargeTopAppBar
import no.nordicsemi.android.nrfmesh.core.ui.MeshTwoLineListItem
import no.nordicsemi.android.nrfmesh.core.ui.SwipeDismissItem
import no.nordicsemi.android.nrfmesh.core.ui.showSnackbar
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.crypto.Utils.encodeHex

@Composable
fun ApplicationKeysRoute(
    viewModel: ApplicationKeysViewModel = hiltViewModel(),
    navigateToApplicationKey: (KeyIndex) -> Unit,
    onBackClicked: () -> Unit
) {
    val uiState: ApplicationKeysScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    ApplicationsKeysScreen(
        uiState = uiState,
        navigateToApplicationKey = navigateToApplicationKey,
        onAddKeyClicked = viewModel::addApplicationKey,
        onDismissed = viewModel::onSwiped,
        onUndoClicked = viewModel::onUndoSwipe
    ) {
        viewModel.removeKeys()
        onBackClicked()
    }
}

@Composable
private fun ApplicationsKeysScreen(
    uiState: ApplicationKeysScreenUiState,
    navigateToApplicationKey: (KeyIndex) -> Unit,
    onAddKeyClicked: () -> ApplicationKey,
    onDismissed: (ApplicationKey) -> Unit,
    onUndoClicked: (ApplicationKey) -> Unit,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(connection = scrollBehavior.nestedScrollConnection),
        topBar = {
            MeshLargeTopAppBar(
                title = stringResource(id = R.string.label_application_keys),
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
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            items(
                items = uiState.keys/*,
                key = { it.index }*/
            ) { key ->
                // Hold the current state from the Swipe to Dismiss composable
                val dismissState = rememberDismissState {
                    val state = if (uiState.keys.size > 1) {
                        val flag = (it == DismissValue.DismissedToStart && !key.isInUse())
                        if (!flag) {
                            showSnackbar(
                                scope = coroutineScope,
                                snackbarHostState = snackbarHostState,
                                message = context.getString(R.string.error_cannot_delete_key_in_use),
                                withDismissAction = true
                            )
                        }
                        flag
                    } else false
                    state
                }
                var keyDismissed by remember { mutableStateOf(false) }
                if (keyDismissed) {
                    showSnackbar(
                        scope = coroutineScope,
                        snackbarHostState = snackbarHostState,
                        message = stringResource(R.string.label_application_key_deleted),
                        actionLabel = stringResource(R.string.action_undo),
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
                        keyDismissed = isDismissed
                        if (isDismissed) {
                            onDismissed(key)
                        }
                        Surface(color = MaterialTheme.colorScheme.background) {
                            MeshTwoLineListItem(
                                modifier = Modifier.clickable {
                                    navigateToApplicationKey(key.index)
                                },
                                leadingIcon = {
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
}