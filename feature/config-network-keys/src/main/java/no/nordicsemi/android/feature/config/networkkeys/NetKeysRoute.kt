@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.feature.config.networkkeys

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.android.nrfmesh.core.ui.*
import no.nordicsemi.android.nrfmesh.feature.config.networkkeys.R
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.crypto.Utils.encodeHex

@Composable
internal fun NetKeysRoute(
    viewModel: NetKeysViewModel = hiltViewModel(),
    navigateToNetworkKeys: () -> Unit
) {
    val uiState: NetKeysScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    NetKeysScreen(
        uiState = uiState,
        navigateToNetworkKeys = navigateToNetworkKeys,
        onAddKeyClicked = viewModel::addNetworkKey,
        onSwiped = viewModel::onSwiped
    )
}

@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
private fun NetKeysScreen(
    uiState: NetKeysScreenUiState,
    navigateToNetworkKeys: () -> Unit,
    onAddKeyClicked: () -> NetworkKey,
    onSwiped: (NetworkKey) -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            AnimatedVisibility(visible = !showBottomSheet) {
                ExtendedFloatingActionButton(
                    text = { Text(stringResource(R.string.label_add_key)) },
                    icon = { Icon(imageVector = Icons.Rounded.Add, contentDescription = null) },
                    onClick = { showBottomSheet = !showBottomSheet }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        when (uiState.netKeysState) {
            NetKeysState.Loading -> MeshLoadingItems(
                imageVector = Icons.Outlined.VpnKey,
                title = stringResource(id = R.string.label_loading)
            )

            is NetKeysState.Success -> {
                NetworkKeys(
                    context = context,
                    coroutineScope = rememberCoroutineScope(),
                    snackbarHostState = snackbarHostState,
                    keys = uiState.netKeysState.netKeys,
                    navigateToNetworkKeys = navigateToNetworkKeys,
                    onSwiped = onSwiped
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
        ModalBottomSheet(onDismissRequest = {
            showBottomSheet = false
        }) {
            BottomSheetTopAppBar(
                navigationIcon = Icons.Outlined.Close,
                onNavigationIconClick = { showBottomSheet = !showBottomSheet },
                title = stringResource(R.string.label_add_key),
            )
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (uiState.keys.isEmpty()) {
                    item {
                        MeshNoItemsAvailable(
                            imageVector = Icons.Outlined.VpnKey,
                            title = stringResource(R.string.label_no_keys_added)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            OutlinedButton(onClick = {
                                navigateToNetworkKeys()
                            }) {
                                Text(text = stringResource(R.string.action_settings))
                            }
                        }
                    }
                } else {
                    items(items = uiState.keys) { key ->
                        ElevatedCardItem(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .clickable {
                                    showBottomSheet = !showBottomSheet
                                },
                            imageVector = Icons.Outlined.VpnKey,
                            title = key.name,
                            subtitle = key.key.encodeHex()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NetworkKeys(
    context: Context,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    keys: List<NetworkKey>,
    navigateToNetworkKeys: () -> Unit,
    onSwiped: (NetworkKey) -> Unit
) {
    val listState = rememberLazyListState()
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
                navigateToNetworkKeys = navigateToNetworkKeys,
                onSwiped = onSwiped
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
    navigateToNetworkKeys: () -> Unit,
    onSwiped: (NetworkKey) -> Unit
) {
    // Hold the current state from the Swipe to Dismiss composable
    var shouldNotDismiss by remember {
        mutableStateOf(false)
    }
    val dismissState = rememberSwipeToDismissState(
        confirmValueChange = {
            shouldNotDismiss = (key.isInUse || key.index.toUInt() == 0.toUInt())
            !shouldNotDismiss
        },
        positionalThreshold = { it * 0.5f }
    )
    SwipeDismissItem(
        dismissState = dismissState,
        content = {
            ElevatedCardItem(
                imageVector = Icons.Outlined.VpnKey,
                title = key.name,
                subtitle = key.key.encodeHex()
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