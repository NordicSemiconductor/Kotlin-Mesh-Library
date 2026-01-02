package no.nordicsemi.android.nrfmesh.feature.config.applicationkeys

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.common.Completed
import no.nordicsemi.android.nrfmesh.core.common.Failed
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.core.common.Utils.describe
import no.nordicsemi.android.nrfmesh.core.ui.MeshMessageStatusDialog
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.android.nrfmesh.core.ui.Row
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
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
    snackbarHostState: SnackbarHostState,
    node: Node,
    messageState: MessageState,
    availableApplicationKeys: List<ApplicationKey>,
    onAddAppKeyClicked: () -> Unit,
    navigateToApplicationKeys: () -> Unit,
    readApplicationKeys: () -> Unit,
    send: (AcknowledgedConfigMessage) -> Unit,
    resetMessageState: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isRefreshing by rememberSaveable {
        mutableStateOf(messageState.isInProgress() && messageState.message is ConfigAppKeyGet)
    }
    val bottomSheetState = rememberModalBottomSheetState()
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        // snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                    .consumeWindowInsets(paddingValues = paddingValues),
                onRefresh = { readApplicationKeys() },
                isRefreshing = isRefreshing
            ) {
                when (node.applicationKeys.isNotEmpty()) {
                    true -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(space = 8.dp),
                    ) {
                        item {
                            SectionTitle(
                                modifier = Modifier.padding(top = 8.dp),
                                title = stringResource(R.string.label_added_application_keys)
                            )
                        }
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
                        item { Spacer(modifier = Modifier.size(size = 16.dp)) }
                    }

                    false -> MeshNoItemsAvailable(
                        modifier = Modifier.fillMaxSize(),
                        imageVector = Icons.Outlined.VpnKey,
                        title = stringResource(R.string.label_no_app_keys_added),
                        rationale = stringResource(R.string.label_no_app_keys_added_rationale)
                    )
                }
            }
        }
    )

    if (showBottomSheet) {
        BottomSheetApplicationKeys(
            bottomSheetState = bottomSheetState,
            messageState = messageState,
            keys = availableApplicationKeys,
            onAppKeyClicked = { send(ConfigAppKeyAdd(key = it)) },
            onAddApplicationKeyClicked = {
                runCatching {
                    onAddAppKeyClicked
                }.onFailure {
                    scope.launch {
                        bottomSheetState.hide()
                        snackbarHostState.showSnackbar(
                            message = it.describe()
                        )
                    }.invokeOnCompletion {
                        if (!bottomSheetState.isVisible) {
                            showBottomSheet = false
                        }
                    }
                }
            },
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
            text = messageState.error.describe(),
            showDismissButton = !messageState.didFail(),
            onDismissRequest = resetMessageState,
        )

        is Completed -> messageState.response?.takeIf {
            it is ConfigStatusMessage
        }?.let {
            when (!(it as ConfigStatusMessage).isSuccess) {
                true -> MeshMessageStatusDialog(
                    text = it.message,
                    showDismissButton = true,
                    onDismissRequest = resetMessageState,
                )

                false -> LaunchedEffect(snackbarHostState) {
                    if (messageState.message is ConfigAppKeyDelete) {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.label_application_key_deleted),
                            duration = SnackbarDuration.Short,
                        ).also {
                            when (it) {
                                SnackbarResult.Dismissed,
                                SnackbarResult.ActionPerformed,
                                    -> resetMessageState()
                            }
                        }
                    } else if (messageState.message is ConfigAppKeyAdd) {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.label_application_key_added),
                            duration = SnackbarDuration.Short,
                        ).also {
                            when (it) {
                                SnackbarResult.Dismissed,
                                SnackbarResult.ActionPerformed,
                                    -> resetMessageState()
                            }
                        }
                    }
                }
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
    val dismissState = rememberSwipeToDismissBoxState()

    SwipeToDismissBox(
        modifier = Modifier.padding(horizontal = 16.dp),
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled,
                    SwipeToDismissBoxValue.StartToEnd,
                    SwipeToDismissBoxValue.EndToStart,
                        -> if (key.isInUse) Color.Gray else Color.Red
                }
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = color, shape = CardDefaults.elevatedShape)
                    .padding(horizontal = 16.dp),
                contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd)
                    Alignment.CenterStart
                else Alignment.CenterEnd
            ) {
                Icon(imageVector = Icons.Outlined.Delete, contentDescription = "null")
            }
        },
        onDismiss = {
            if (key.isInUse) {
                scope.launch {
                    dismissState.reset()
                }
                snackbarHostState.currentSnackbarData?.dismiss()
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = context.getString(
                            R.string.label_app_key_in_use,
                            key.name
                        ),
                        duration = SnackbarDuration.Short,
                    )
                }
            } else {
                onSwiped(key)
                snackbarHostState.currentSnackbarData?.dismiss()
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.label_application_key_deleted),
                        duration = SnackbarDuration.Short,
                    )
                }
            }
        },
        content = { key.Row() }
    )
}