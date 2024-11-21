package no.nordicsemi.android.nrfmesh.feature.config.applicationkeys

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.nrfmesh.core.common.Failed
import no.nordicsemi.android.nrfmesh.core.common.NotStarted.didFail
import no.nordicsemi.android.nrfmesh.core.common.NotStarted.isInProgress
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.ui.BottomSheetTopAppBar
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.MeshAlertDialog
import no.nordicsemi.android.nrfmesh.core.ui.MeshLoadingItems
import no.nordicsemi.android.nrfmesh.core.ui.MeshMessageStatusDialog
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.android.nrfmesh.core.ui.SwipeDismissItem
import no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.navigation.ConfigAppKeysScreen
import no.nordicsemi.kotlin.data.toHexString
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.Node

@Composable
internal fun ConfigAppKeysRoute(
    appState: AppState,
    uiState: AppKeysScreenUiState,
    navigateToNetworkKeys: () -> Unit,
    onAddKeyClicked: (ApplicationKey) -> Unit,
    onSwiped: (ApplicationKey) -> Unit,
    onRefresh: () -> Unit,
    resetMessageState: () -> Unit,
    onBackPressed: () -> Unit
) {
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    val screen = appState.currentScreen as? ConfigAppKeysScreen
    LaunchedEffect(key1 = screen) {
        screen?.buttons?.onEach { button ->
            when (button) {
                ConfigAppKeysScreen.Actions.ADD_KEY -> showBottomSheet = !showBottomSheet
                ConfigAppKeysScreen.Actions.BACK -> if (!uiState.messageState.isInProgress()) {
                    onBackPressed()
                }
            }
        }?.launchIn(this)
    }

    BackHandler(enabled = uiState.messageState.isInProgress(), onBack = { })
    ConfigAppKeysRoute(
        uiState = uiState,
        showBottomSheet = showBottomSheet,
        dismissBottomSheet = { showBottomSheet = !showBottomSheet },
        navigateToNetworkKeys = navigateToNetworkKeys,
        onAddKeyClicked = onAddKeyClicked,
        onSwiped = onSwiped,
        onRefresh = onRefresh,
        resetMessageState = resetMessageState
    )
}


@Composable
private fun ConfigAppKeysRoute(
    uiState: AppKeysScreenUiState,
    showBottomSheet: Boolean,
    dismissBottomSheet: () -> Unit,
    onSwiped: (ApplicationKey) -> Unit,
    navigateToNetworkKeys: () -> Unit,
    onAddKeyClicked: (ApplicationKey) -> Unit,
    onRefresh: () -> Unit,
    resetMessageState: () -> Unit,
) {
    Column {
        AnimatedVisibility(visible = uiState.messageState.isInProgress()) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        when (uiState.appKeysState) {
            AppKeysState.Loading -> MeshLoadingItems(
                imageVector = Icons.Outlined.VpnKey,
                title = stringResource(id = R.string.label_loading)
            )

            is AppKeysState.Success -> {
                ApplicationKeysInfo(
                    node = uiState.node,
                    keys = uiState.appKeysState.appKeys,
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = onRefresh,
                    onSwiped = onSwiped
                )
            }

            is AppKeysState.Error -> {
                MeshNoItemsAvailable(
                    imageVector = Icons.Outlined.VpnKey,
                    title = stringResource(R.string.label_no_app_keys_to_add)
                )
            }
        }
    }
    if (showBottomSheet) {
        BottomSheetKeys(
            uiState = uiState,
            onAddKeyClicked = onAddKeyClicked,
            navigateToNetworkKeys = navigateToNetworkKeys,
            onDismissClick = dismissBottomSheet
        )
    }

    when (uiState.messageState) {
        is Failed -> MeshMessageStatusDialog(
            text = uiState.messageState.error.message ?: stringResource(R.string.unknown_error),
            showDismissButton = !uiState.messageState.didFail(),
            onDismissRequest = resetMessageState,
        )

        else -> {}
    }
}

@OptIn(ExperimentalStdlibApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetKeys(
    uiState: AppKeysScreenUiState,
    onAddKeyClicked: (ApplicationKey) -> Unit,
    navigateToNetworkKeys: () -> Unit,
    onDismissClick: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismissClick) {
        BottomSheetTopAppBar(title = stringResource(R.string.label_add_key))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            if (uiState.keys.isEmpty()) {
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
                items(items = uiState.keys) { key ->
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

@Composable
private fun ApplicationKeysInfo(
    node: Node?,
    keys: List<ApplicationKey>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onSwiped: (ApplicationKey) -> Unit
) {
    when (keys.isNotEmpty()) {
        true -> {
            ApplicationKeys(
                node = node,
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                keys = keys,
                onSwiped = onSwiped
            )
        }

        else -> MeshNoItemsAvailable(
            imageVector = Icons.Outlined.VpnKey,
            title = stringResource(R.string.label_no_app_keys_added)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApplicationKeys(
    node: Node?,
    keys: List<ApplicationKey>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onSwiped: (ApplicationKey) -> Unit
) {
    val listState = rememberLazyListState()
    val state = rememberPullToRefreshState()
    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        state = state,
        onRefresh = onRefresh,
        isRefreshing = isRefreshing
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            items(items = keys) { key ->
                SwipeToDismissKey(
                    node = node,
                    key = key,
                    onSwiped = onSwiped
                )
            }
        }
    }
}


@OptIn(ExperimentalStdlibApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissKey(
    node: Node?,
    key: ApplicationKey,
    onSwiped: (ApplicationKey) -> Unit
) {
    // Hold the current state from the Swipe to Dismiss composable
    var isKeyInUse by remember { mutableStateOf(false) }
    var displayWarningDialog by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            // Check if the key is in use and prevent dismissing if it is in use.
            isKeyInUse = node?.elements?.flatMap { it.models }?.any { model ->
                model.bind.contains(key.index)
            } ?: false
            displayWarningDialog = isKeyInUse
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
            onDismissClick = { displayWarningDialog = false },
            confirmButtonText = stringResource(R.string.label_ok),
            onConfirmClick = {
                displayWarningDialog = false
                onSwiped(key)
            }
        )
    }
}