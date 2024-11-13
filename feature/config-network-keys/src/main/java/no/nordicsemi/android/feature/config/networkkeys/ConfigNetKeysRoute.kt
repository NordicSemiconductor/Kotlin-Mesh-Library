package no.nordicsemi.android.feature.config.networkkeys

import android.content.Context
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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.feature.config.networkkeys.navigation.ConfigNetKeysScreen
import no.nordicsemi.android.nrfmesh.core.common.Completed
import no.nordicsemi.android.nrfmesh.core.common.Failed
import no.nordicsemi.android.nrfmesh.core.common.NotStarted.didFail
import no.nordicsemi.android.nrfmesh.core.common.NotStarted.isInProgress
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.ui.BottomSheetTopAppBar
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.MeshLoadingItems
import no.nordicsemi.android.nrfmesh.core.ui.MeshMessageStatusDialog
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.android.nrfmesh.core.ui.SwipeDismissItem
import no.nordicsemi.android.nrfmesh.core.ui.isDismissed
import no.nordicsemi.android.nrfmesh.core.ui.showSnackbar
import no.nordicsemi.android.nrfmesh.feature.config.networkkeys.R
import no.nordicsemi.kotlin.data.toHexString
import no.nordicsemi.kotlin.mesh.core.messages.StatusMessage
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey

@Composable
internal fun ConfigNetKeysRoute(
    appState: AppState,
    uiState: NetKeysScreenUiState,
    navigateToNetworkKeys: () -> Unit,
    onAddKeyClicked: (NetworkKey) -> Unit,
    onSwiped: (NetworkKey) -> Unit,
    onRefresh: () -> Unit,
    resetMessageState: () -> Unit,
    onBackPressed: () -> Unit
) {
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    val screen = appState.currentScreen as? ConfigNetKeysScreen
    LaunchedEffect(key1 = screen) {
        screen?.buttons?.onEach { button ->
            when (button) {
                ConfigNetKeysScreen.Actions.ADD_KEY -> showBottomSheet = !showBottomSheet
                ConfigNetKeysScreen.Actions.BACK -> if (!uiState.messageState.isInProgress()) {
                    onBackPressed()
                }
            }
        }?.launchIn(this)
    }

    BackHandler(enabled = uiState.messageState.isInProgress(), onBack = { })
    ConfigNetKeysScreen(
        uiState = uiState,
        snackbarHostState = appState.snackbarHostState,
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
private fun ConfigNetKeysScreen(
    uiState: NetKeysScreenUiState,
    snackbarHostState: SnackbarHostState,
    showBottomSheet: Boolean,
    dismissBottomSheet: () -> Unit,
    navigateToNetworkKeys: () -> Unit,
    onAddKeyClicked: (NetworkKey) -> Unit,
    onSwiped: (NetworkKey) -> Unit,
    onRefresh: () -> Unit,
    resetMessageState: () -> Unit,
) {
    val context = LocalContext.current
    Column {
        AnimatedVisibility(visible = uiState.messageState.isInProgress()) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        when (uiState.netKeysState) {
            NetKeysState.Loading -> MeshLoadingItems(
                imageVector = Icons.Outlined.VpnKey,
                title = stringResource(id = R.string.label_loading)
            )

            is NetKeysState.Success -> {
                NetworkKeysInfo(
                    context = context,
                    coroutineScope = rememberCoroutineScope(),
                    snackbarHostState = snackbarHostState,
                    keys = uiState.netKeysState.netKeys,
                    onSwiped = onSwiped,
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = onRefresh
                )
            }

            is NetKeysState.Error -> {
                MeshNoItemsAvailable(
                    imageVector = Icons.Outlined.VpnKey,
                    title = stringResource(R.string.label_no_keys_added)
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
        is Failed -> {
            MeshMessageStatusDialog(
                text = uiState.messageState.error.message ?: stringResource(R.string.unknown_error),
                showDismissButton = !uiState.messageState.didFail(),
                onDismissRequest = resetMessageState,
            )
        }

        is Completed -> {
            uiState.messageState.response?.let {
                MeshMessageStatusDialog(
                    text = when (it) {
                        is StatusMessage -> it.message
                        else -> stringResource(id = R.string.label_success)
                    },
                    showDismissButton = uiState.messageState.didFail(),
                    onDismissRequest = resetMessageState,
                )
            }
        }

        else -> {

        }
    }
}

@OptIn(ExperimentalStdlibApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetKeys(
    uiState: NetKeysScreenUiState,
    onAddKeyClicked: (NetworkKey) -> Unit,
    navigateToNetworkKeys: () -> Unit,
    onDismissClick: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismissClick) {
        BottomSheetTopAppBar(
            navigationIcon = Icons.Outlined.Close,
            onNavigationIconClick = onDismissClick,
            title = stringResource(R.string.label_add_key)
        )
        LazyColumn(verticalArrangement = Arrangement.SpaceAround) {
            if (uiState.keys.isEmpty()) {
                item {
                    MeshNoItemsAvailable(
                        imageVector = Icons.Outlined.VpnKey,
                        title = stringResource(R.string.label_no_keys_added)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedButton(onClick = { navigateToNetworkKeys() }) {
                            Text(text = stringResource(R.string.action_settings))
                        }
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
private fun NetworkKeysInfo(
    context: Context,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    keys: List<NetworkKey>,
    onSwiped: (NetworkKey) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    when (keys.isNotEmpty()) {
        true -> NetworkKeys(
            context = context,
            coroutineScope = coroutineScope,
            snackbarHostState = snackbarHostState,
            keys = keys,
            onSwiped = onSwiped,
            isRefreshing = isRefreshing,
            onRefresh = onRefresh
        )

        false -> MeshNoItemsAvailable(
            imageVector = Icons.Outlined.VpnKey,
            title = context.getString(R.string.label_no_keys_added)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NetworkKeys(
    context: Context,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    keys: List<NetworkKey>,
    onSwiped: (NetworkKey) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
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
                    key = key,
                    context = context,
                    coroutineScope = coroutineScope,
                    snackbarHostState = snackbarHostState,
                    onSwiped = onSwiped
                )
            }
        }
    }
}

@OptIn(ExperimentalStdlibApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissKey(
    key: NetworkKey,
    context: Context,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onSwiped: (NetworkKey) -> Unit
) {
    // Hold the current state from the Swipe to Dismiss composable
    var shouldNotDismiss by remember {
        mutableStateOf(false)
    }
    val dismissState = rememberSwipeToDismissBoxState(
        /*confirmValueChange = {
            shouldNotDismiss = (key.isInUse || key.index.toUInt() == 0.toUInt())
            !shouldNotDismiss
        },*/
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

    if (shouldNotDismiss) {
        LaunchedEffect(snackbarHostState) {
            showSnackbar(
                scope = coroutineScope,
                snackbarHostState = snackbarHostState,
                message = context.getString(
                    if (key.index.toUInt() == 0.toUInt())
                        R.string.error_cannot_delete_primary_network_key
                    else
                        R.string.error_cannot_delete_key_in_use
                ),
                duration = SnackbarDuration.Short,
                onDismissed = { shouldNotDismiss = false }
            )
        }
    }
    if (dismissState.isDismissed()) {
        LaunchedEffect(snackbarHostState) {
            onSwiped(key)
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.label_network_key_deleted),
                actionLabel = context.getString(R.string.action_undo),
                withDismissAction = true,
                duration = SnackbarDuration.Long,
            )
        }
    }
}