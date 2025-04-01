package no.nordicsemi.android.nrfmesh.feature.config.applicationkeys

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.common.Completed
import no.nordicsemi.android.nrfmesh.core.common.Failed
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.core.ui.MeshMessageStatusDialog
import no.nordicsemi.android.nrfmesh.core.ui.Row
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.core.ui.SwipeDismissItem
import no.nordicsemi.android.nrfmesh.core.ui.isDismissed
import no.nordicsemi.android.nrfmesh.core.ui.showSnackbar
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigStatusMessage
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigAppKeyAdd
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigAppKeyDelete
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigAppKeyGet
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.Node

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ConfigAppKeysScreen(
    node: Node,
    messageState: MessageState,
    availableApplicationKeys: List<ApplicationKey>,
    onAddAppKeyClicked: () -> Unit,
    navigateToApplicationKeys: () -> Unit,
    send: (AcknowledgedConfigMessage) -> Unit,
    resetMessageState: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isRefreshing by rememberSaveable {
        mutableStateOf(messageState.isInProgress() && messageState.message is ConfigAppKeyGet)
    }
    val bottomSheetState = rememberModalBottomSheetState()
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets(top = 0.dp),
        floatingActionButton = {
            AnimatedVisibility(visible = !showBottomSheet) {
                ExtendedFloatingActionButton(
                    modifier = Modifier.defaultMinSize(minWidth = 150.dp),
                    text = { Text(text = stringResource(R.string.label_add_key)) },
                    icon = { Icon(imageVector = Icons.Outlined.Add, contentDescription = null) },
                    onClick = { showBottomSheet = true },
                    expanded = true
                )
            }
        },
        content = { paddingValues ->
            PullToRefreshBox(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = paddingValues),
                onRefresh = {
                    send(
                        ConfigAppKeyGet(
                            key = node.networkKeys.firstOrNull()
                                ?: throw IllegalStateException("No network keys added to this node")
                        )
                    )
                },
                isRefreshing = isRefreshing
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    SectionTitle(
                        modifier = Modifier.padding(vertical = 8.dp),
                        title = stringResource(R.string.label_added_application_keys)
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
                    ) {
                        items(
                            items = node.applicationKeys,
                            key = { it.index.toInt() + 1 }
                        ) { key ->
                            SwipeToDismissKey(
                                key = key,
                                context = context,
                                scope = scope,
                                snackbarHostState = snackbarHostState,
                                onSwiped = {
                                    if (!messageState.isInProgress())
                                        send(ConfigAppKeyDelete(key = key))
                                }
                            )
                        }
                    }
                }

            }
        }
    )

    if (showBottomSheet) {
        BottomSheetApplicationKeys(
            bottomSheetState = bottomSheetState,
            keys = availableApplicationKeys,
            onAppKeyClicked = {
                send(ConfigAppKeyAdd(key = it))
                scope.launch {
                    bottomSheetState.hide()
                }.invokeOnCompletion {
                    if (!bottomSheetState.isVisible) {
                        showBottomSheet = false
                    }
                }
            },
            onAddApplicationKeyClicked = onAddAppKeyClicked,
            navigateToApplicationKeys = {
                scope.launch {
                    bottomSheetState.hide()
                }.invokeOnCompletion {
                    if (!bottomSheetState.isVisible) {
                        showBottomSheet = false
                    }
                }
                navigateToApplicationKeys()
            },
            onDismissClick = {
                scope.launch {
                    bottomSheetState.hide()
                }.invokeOnCompletion {
                    if (!bottomSheetState.isVisible) {
                        showBottomSheet = false
                    }
                }
            }
        )
    }
    when (messageState) {
        is Failed -> MeshMessageStatusDialog(
            text = messageState.error.message ?: stringResource(R.string.unknown_error),
            showDismissButton = !messageState.didFail(),
            onDismissRequest = resetMessageState,
        )

        is Completed -> {
            messageState.response?.takeIf {
                (it is ConfigStatusMessage && !it.isSuccess)
            }?.let {
                MeshMessageStatusDialog(
                    text = (messageState.response as ConfigStatusMessage).message,
                    showDismissButton = true,
                    onDismissRequest = resetMessageState,
                )
            }
        }

        else -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissKey(
    key: ApplicationKey,
    context: Context,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onSwiped: (ApplicationKey) -> Unit,
) {
    // Hold the current state from the Swipe to Dismiss composable
    var shouldNotDismiss by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            onSwiped(key)
            true
        },
        positionalThreshold = { it * 0.5f }
    )
    SwipeDismissItem(dismissState = dismissState, content = { key.Row() })

    if (shouldNotDismiss) {
        LaunchedEffect(snackbarHostState) {
            showSnackbar(
                scope = scope,
                snackbarHostState = snackbarHostState,
                message = context.getString(R.string.error_cannot_delete_key_in_use),
                duration = SnackbarDuration.Short,
                onDismissed = { shouldNotDismiss = false }
            )
        }
    }
    if (dismissState.isDismissed()) {
        LaunchedEffect(snackbarHostState) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.label_application_key_deleted),
                    duration = SnackbarDuration.Short,
                )
            }
        }
    }
}