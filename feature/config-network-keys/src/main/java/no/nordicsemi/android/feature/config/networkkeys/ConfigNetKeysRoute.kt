package no.nordicsemi.android.feature.config.networkkeys

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
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
import no.nordicsemi.android.nrfmesh.core.common.Completed
import no.nordicsemi.android.nrfmesh.core.common.Failed
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.MeshMessageStatusDialog
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.core.ui.showSnackbar
import no.nordicsemi.android.nrfmesh.feature.config.networkkeys.R
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigStatusMessage
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNetKeyAdd
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNetKeyDelete
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNetKeyGet
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.Node

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ConfigNetKeysRoute(
    node: Node,
    messageState: MessageState,
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
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets(top = 0.dp),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier.defaultMinSize(minWidth = 150.dp),
                text = { Text(text = stringResource(R.string.label_network_keys)) },
                icon = { Icon(imageVector = Icons.Outlined.VpnKey, contentDescription = null) },
                onClick = navigateToNetworkKeys,
                expanded = true
            )
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
                    node.network?.networkKeys.orEmpty().let { keys ->
                        when (keys.isNotEmpty()) {
                            true -> LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(space = 8.dp)
                            ) {
                                items(items = keys, key = { it.index.toInt() + 1 }) { key ->
                                    KeyRow(
                                        node = node,
                                        key = key,
                                        context = context,
                                        scope = scope,
                                        snackbarHostState = snackbarHostState,
                                        send = send
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
        }
    )

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

@Composable
private fun KeyRow(
    node: Node,
    key: NetworkKey,
    context: Context,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    send: (AcknowledgedConfigMessage) -> Unit,
) {
    var isAdded by rememberSaveable { mutableStateOf(node.knows(key = key)) }
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.VpnKey,
        title = key.name,
        titleAction = {
            Checkbox(
                checked = isAdded,
                onCheckedChange = {
                    if (!isAdded) {
                        send(ConfigNetKeyAdd(key = key))
                    } else {
                        // Check if the key is in use before unbinding.
                        if (node.networkKeys.size == 1 && key.isInUse) {
                            showSnackbar(
                                scope = scope,
                                snackbarHostState = snackbarHostState,
                                message = context.getString(
                                    R.string.error_cannot_last_network_key_or_key_in_use
                                ),
                                duration = SnackbarDuration.Short
                            )
                        }
                        runCatching {
                            send(ConfigNetKeyDelete(key = key))
                        }.onFailure { throwable ->
                            isAdded = node.knows(key = key)
                            showSnackbar(
                                scope = scope,
                                snackbarHostState = snackbarHostState,
                                message = throwable.message
                                    ?: context.getString(R.string.unknown_error),
                                duration = SnackbarDuration.Short
                            )
                        }

                    }
                    isAdded = it
                }
            )
        }
    )
}