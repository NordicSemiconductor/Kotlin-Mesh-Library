package no.nordicsemi.android.feature.config.networkkeys

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
import androidx.compose.material.icons.outlined.VpnKey
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
import no.nordicsemi.android.nrfmesh.core.common.Utils.describe
import no.nordicsemi.android.nrfmesh.core.ui.MeshMessageStatusDialog
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.android.nrfmesh.core.ui.Row
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.core.ui.SwipeDismissItem
import no.nordicsemi.android.nrfmesh.core.ui.showSnackbar
import no.nordicsemi.android.nrfmesh.feature.config.networkkeys.R
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigStatusMessage
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNetKeyAdd
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNetKeyDelete
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNetKeyGet
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ConfigNetKeysScreen(
    addedNetworkKeys: List<NetworkKey>,
    availableNetworkKeys: List<NetworkKey>,
    messageState: MessageState,
    onAddNetworkKeyClicked: () -> Unit,
    navigateToNetworkKeys: () -> Unit,
    send: (AcknowledgedConfigMessage) -> Unit,
    resetMessageState: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isRefreshing by rememberSaveable {
        mutableStateOf(messageState.isInProgress() && messageState.message is ConfigNetKeyGet)
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
                onRefresh = { send(ConfigNetKeyGet()) },
                isRefreshing = isRefreshing
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    SectionTitle(
                        modifier = Modifier.padding(vertical = 8.dp),
                        title = stringResource(R.string.label_added_network_keys)
                    )
                    when (addedNetworkKeys.isNotEmpty()) {
                        true -> LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(space = 8.dp)
                        ) {
                            items(items = addedNetworkKeys, key = { it.index.toInt() + 1 }) { key ->
                                SwipeToDismissKey(
                                    key = key,
                                    context = context,
                                    scope = scope,
                                    snackbarHostState = snackbarHostState,
                                    onSwiped = {
                                        if (!messageState.isInProgress())
                                            send(ConfigNetKeyDelete(key = key))
                                    }
                                )
                            }
                        }

                        false -> MeshNoItemsAvailable(
                            imageVector = Icons.Outlined.VpnKey,
                            title = context.getString(R.string.label_no_keys_added)
                        )
                    }
                }
            }
        }
    )

    if (showBottomSheet) {
        BottomSheetNetworkKeys(
            bottomSheetState = bottomSheetState,
            messageState = messageState,
            keys = availableNetworkKeys,
            onNetworkKeyClicked = { send(ConfigNetKeyAdd(key = it)) },
            onAddNetworkKeyClicked = onAddNetworkKeyClicked,
            navigateToNetworkKeys = {
                scope.launch {
                    bottomSheetState.hide()
                }.invokeOnCompletion {
                    navigateToNetworkKeys()
                    if (!bottomSheetState.isVisible) {
                        showBottomSheet = false
                    }
                }
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
                    if (messageState.message is ConfigNetKeyDelete) {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.label_network_key_deleted),
                            duration = SnackbarDuration.Short,
                        )
                    } else if (messageState.message is ConfigNetKeyAdd) {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.label_network_key_added),
                            duration = SnackbarDuration.Short,
                        )
                    }
                }
            }
        }

        else -> { /* Do nothing */ }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissKey(
    key: NetworkKey,
    context: Context,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onSwiped: (NetworkKey) -> Unit,
) {
    // Hold the current state from the Swipe to Dismiss composable
    var shouldNotDismiss by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (key.index.toUInt() == 0.toUInt()) {
                shouldNotDismiss = true
                false
            } else {
                onSwiped(key)
                true
            }
        },
        positionalThreshold = { it * 0.5f }
    )
    SwipeDismissItem(
        dismissState = dismissState, content = { key.Row() }
    )

    if (shouldNotDismiss) {
        LaunchedEffect(snackbarHostState) {
            showSnackbar(
                scope = scope,
                snackbarHostState = snackbarHostState,
                message = context.getString(R.string.error_cannot_delete_primary_network_key),
                duration = SnackbarDuration.Short,
                onDismissed = { shouldNotDismiss = false }
            )
        }
    }
}