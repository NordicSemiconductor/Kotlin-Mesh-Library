package no.nordicsemi.android.nrfmesh.feature.network.keys

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.android.nrfmesh.core.ui.SwipeDismissItem
import no.nordicsemi.android.nrfmesh.core.ui.isDismissed
import no.nordicsemi.android.nrfmesh.core.ui.showSnackbar
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeysScreen
import no.nordicsemi.kotlin.data.toHexString
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey

@Composable
internal fun NetworkKeysRoute(
    appState: AppState,
    uiState: NetworkKeysScreenUiState,
    navigateToKey: (KeyIndex) -> Unit,
    onAddKeyClicked: () -> NetworkKey,
    onSwiped: (NetworkKey) -> Unit,
    onUndoClicked: (NetworkKey) -> Unit,
    remove: (NetworkKey) -> Unit,
    onBackPressed: () -> Unit
) {
    val screen = appState.currentScreen as? NetworkKeysScreen
    LaunchedEffect(key1 = screen) {
        screen?.buttons?.onEach { button ->
            when (button) {
                NetworkKeysScreen.Actions.ADD_KEY -> navigateToKey(onAddKeyClicked().index)
                NetworkKeysScreen.Actions.BACK -> onBackPressed()
            }

        }?.launchIn(this)
    }
    NetworkKeysScreen(
        snackbarHostState = appState.snackbarHostState,
        uiState = uiState,
        navigateToKey = navigateToKey,
        onSwiped = onSwiped,
        onUndoClicked = onUndoClicked,
        remove = remove
    )
}

@Composable
private fun NetworkKeysScreen(
    snackbarHostState: SnackbarHostState,
    uiState: NetworkKeysScreenUiState,
    navigateToKey: (KeyIndex) -> Unit,
    onSwiped: (NetworkKey) -> Unit,
    onUndoClicked: (NetworkKey) -> Unit,
    remove: (NetworkKey) -> Unit
) {
    val context = LocalContext.current
    when (uiState.keys.isEmpty()) {
        true -> MeshNoItemsAvailable(
            imageVector = Icons.Outlined.VpnKey,
            title = stringResource(R.string.label_no_keys_added)
        )

        false -> NetworkKeys(
            context = context,
            coroutineScope = rememberCoroutineScope(),
            snackbarHostState = snackbarHostState,
            keys = uiState.keys,
            navigateToKey = navigateToKey,
            onSwiped = onSwiped,
            onUndoClicked = onUndoClicked,
            remove = remove
        )
    }
}

@Composable
private fun NetworkKeys(
    context: Context,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    keys: List<NetworkKey>,
    navigateToKey: (KeyIndex) -> Unit,
    onSwiped: (NetworkKey) -> Unit,
    onUndoClicked: (NetworkKey) -> Unit,
    remove: (NetworkKey) -> Unit
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
                navigateToNetworkKey = navigateToKey,
                onSwiped = onSwiped,
                onUndoClicked = onUndoClicked,
                remove = remove
            )
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
    navigateToNetworkKey: (KeyIndex) -> Unit,
    onSwiped: (NetworkKey) -> Unit,
    onUndoClicked: (NetworkKey) -> Unit,
    remove: (NetworkKey) -> Unit
) {
    // Hold the current state from the Swipe to Dismiss composable
    var shouldNotDismiss by remember {
        mutableStateOf(false)
    }
    val dismissState = rememberSwipeToDismissBoxState(
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
                onClick = { navigateToNetworkKey(key.index) },
                imageVector = Icons.Outlined.VpnKey,
                title = key.name,
                subtitle = key.key.toHexString()
            )
        }
    )

    if (shouldNotDismiss) {
        LaunchedEffect(snackbarHostState) {
            Log.d("AAA", "Show snackbar?")
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
            Log.d("AAA", "Did snackbar appear?")
        }
    }
    if (dismissState.isDismissed()) {
        LaunchedEffect(snackbarHostState) {
            onSwiped(key)
            showSnackbar(
                scope = coroutineScope,
                snackbarHostState = snackbarHostState,
                message = context.getString(R.string.label_network_key_deleted),
                actionLabel = context.getString(R.string.action_undo),
                withDismissAction = true,
                duration = SnackbarDuration.Long,
                onDismissed = { remove(key) },
                onActionPerformed = { onUndoClicked(key) }
            )
        }
    }
}