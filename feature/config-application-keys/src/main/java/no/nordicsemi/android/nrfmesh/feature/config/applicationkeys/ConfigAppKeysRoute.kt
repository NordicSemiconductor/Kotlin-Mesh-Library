package no.nordicsemi.android.nrfmesh.feature.config.applicationkeys

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import no.nordicsemi.android.nrfmesh.core.ui.BottomSheetTopAppBar
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.MeshAlertDialog
import no.nordicsemi.android.nrfmesh.core.ui.MeshMessageStatusDialog
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.core.ui.SwipeDismissItem
import no.nordicsemi.android.nrfmesh.core.ui.isDismissed
import no.nordicsemi.kotlin.data.toHexString
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigStatusMessage
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigAppKeyDelete
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigAppKeyGet
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.Element
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ConfigAppKeysRoute(
    elements: List<Element>,
    networkKeys: List<NetworkKey>,
    applicationKeys: List<ApplicationKey>,
    messageState: MessageState,
    onApplicationKeysClicked: () -> Unit,
    onAddKeyClicked: (ApplicationKey) -> Unit,
    send: (AcknowledgedConfigMessage) -> Unit,
    resetMessageState: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isRefreshing by rememberSaveable {
        mutableStateOf(messageState.isInProgress() && messageState.message is ConfigAppKeyGet)
    }
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets(top = 0.dp),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier.defaultMinSize(minWidth = 150.dp),
                text = { Text(text = stringResource(R.string.label_application_keys)) },
                icon = { Icon(imageVector = Icons.Outlined.VpnKey, contentDescription = null) },
                onClick = onApplicationKeysClicked,
                expanded = true
            )
        },
        content = { paddingValues ->
            PullToRefreshBox(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = paddingValues),
                onRefresh = {
                    send(
                        ConfigAppKeyGet(
                            key = networkKeys.firstOrNull()
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
                    when (applicationKeys.isNotEmpty()) {
                        true -> LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(space = 8.dp)
                        ) {
                            items(items = applicationKeys, key = { it.index.toInt() + 1 }) { key ->
                                SwipeToDismissKey(
                                    context = context,
                                    scope = scope,
                                    elements = elements,
                                    key = key,
                                    snackbarHostState = snackbarHostState,
                                    onSwiped = { send(ConfigAppKeyDelete(key = it)) }
                                )
                            }
                        }

                        else -> MeshNoItemsAvailable(
                            imageVector = Icons.Outlined.VpnKey,
                            title = stringResource(R.string.label_no_app_keys_added),
                            rationale = stringResource(R.string.label_no_app_keys_to_add_rationale)
                        )
                    }
                }

            }
        }
    )
    if (showBottomSheet) {
        BottomSheetKeys(
            appKeys = applicationKeys,
            onAddKeyClicked = onAddKeyClicked,
            navigateToNetworkKeys = onApplicationKeysClicked,
            onDismissClick = { showBottomSheet = false }
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

@OptIn(ExperimentalStdlibApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetKeys(
    appKeys: List<ApplicationKey>,
    onAddKeyClicked: (ApplicationKey) -> Unit,
    navigateToNetworkKeys: () -> Unit,
    onDismissClick: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismissClick) {
        BottomSheetTopAppBar(title = stringResource(R.string.label_add_key))
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            if (appKeys.isEmpty()) {
                item {
                    MeshNoItemsAvailable(
                        imageVector = Icons.Outlined.VpnKey,
                        title = stringResource(R.string.label_no_app_keys_to_add)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedButton(
                            onClick = { navigateToNetworkKeys() },
                            content = { Text(text = stringResource(R.string.action_settings)) }
                        )
                    }
                }
            } else {
                items(items = appKeys) { key ->
                    ElevatedCardItem(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onClick = {
                            onDismissClick()
                            onAddKeyClicked(key)
                        },
                        imageVector = Icons.Outlined.VpnKey,
                        title = key.name,
                        subtitle = key.key.toHexString()
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalStdlibApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissKey(
    elements: List<Element>,
    context: Context,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    key: ApplicationKey,
    onSwiped: (ApplicationKey) -> Unit,
) {
    // Hold the current state from the Swipe to Dismiss composable
    var displayWarningDialog by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            // Check if the key is in use and prevent dismissing if it is in use.
            val isKeyInUse = elements.flatMap { it.models }.any { model ->
                model.bind.contains(key.index)
            }
            displayWarningDialog = isKeyInUse
            if(!isKeyInUse) {
                onSwiped(key)
            }
            !isKeyInUse
        },
        positionalThreshold = { it * 0.5f }
    )
    SwipeDismissItem(
        dismissState = dismissState,
        content = {
            ElevatedCardItem(
                imageVector = Icons.Outlined.VpnKey,
                title = key.name,
                subtitle = key.key.toHexString()
            )
        }
    )
    if (displayWarningDialog) {
        MeshAlertDialog(
            onDismissRequest = { displayWarningDialog = !displayWarningDialog },
            icon = Icons.Outlined.Warning,
            iconColor = MaterialTheme.colorScheme.error,
            title = stringResource(R.string.warning),
            text = stringResource(R.string.warning_key_is_in_use),
            dismissButtonText = stringResource(R.string.label_cancel),
            onDismissClick = { displayWarningDialog = !displayWarningDialog },
            confirmButtonText = stringResource(R.string.label_ok),
            onConfirmClick = {
                displayWarningDialog = !displayWarningDialog
                onSwiped(key)
            }
        )
    }
    LaunchedEffect(snackbarHostState) {
        if (dismissState.isDismissed()) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.label_application_key_deleted),
                    duration = SnackbarDuration.Short,
                )
            }
        }
    }
}