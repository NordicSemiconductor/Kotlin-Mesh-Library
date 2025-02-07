package no.nordicsemi.android.nrfmesh.feature.network.keys

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.models.NetworkKeyData
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.core.ui.SwipeDismissItem
import no.nordicsemi.android.nrfmesh.core.ui.isDismissed
import no.nordicsemi.kotlin.data.toHexString
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey

@Composable
internal fun NetworkKeysRoute(
    highlightSelectedItem: Boolean,
    onAddKeyClicked: () -> NetworkKey,
    networkKeys: List<NetworkKeyData>,
    navigateToKey: (KeyIndex) -> Unit,
    onSwiped: (NetworkKeyData) -> Unit,
    onUndoClicked: (NetworkKeyData) -> Unit,
    remove: (NetworkKeyData) -> Unit,
) {
    NetworkKeys(
        highlightSelectedItem = highlightSelectedItem,
        keys = networkKeys,
        onAddKeyClicked = onAddKeyClicked,
        navigateToKey = navigateToKey,
        onSwiped = onSwiped,
        onUndoClicked = onUndoClicked,
        remove = remove
    )
}

@Composable
private fun NetworkKeys(
    highlightSelectedItem: Boolean,
    keys: List<NetworkKeyData>,
    onAddKeyClicked: () -> NetworkKey,
    navigateToKey: (KeyIndex) -> Unit,
    onSwiped: (NetworkKeyData) -> Unit,
    onUndoClicked: (NetworkKeyData) -> Unit,
    remove: (NetworkKeyData) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedKeyIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    Scaffold(
        modifier = Modifier.background(color = Color.Red),
        contentWindowInsets = WindowInsets(top = 8.dp),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier.defaultMinSize(minWidth = 150.dp),
                text = { Text(text = stringResource(R.string.label_add_key)) },
                icon = { Icon(imageVector = Icons.Outlined.Add, contentDescription = null) },
                onClick = {
                    runCatching {
                        onAddKeyClicked()
                    }.onSuccess {
                        selectedKeyIndex = it.index.toInt()
                        navigateToKey(it.index)
                    }
                },
                expanded = true
            )
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            item { SectionTitle(title = stringResource(R.string.label_network_keys)) }
            items(items = keys, key = { (it.index + 1u).toInt() }) { key ->
                val isSelected = highlightSelectedItem && key.index.toInt() == selectedKeyIndex
                SwipeToDismissKey(
                    scope = scope,
                    context = context,
                    snackbarHostState = snackbarHostState,
                    key = key,
                    isSelected = isSelected,
                    navigateToNetworkKey = {
                        selectedKeyIndex = it.toInt()
                        navigateToKey(it)
                    },
                    onSwiped = onSwiped,
                    onUndoClicked = onUndoClicked,
                    remove = remove
                )
            }
        }
    }
}

@OptIn(ExperimentalStdlibApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissKey(
    scope: CoroutineScope,
    context: Context,
    snackbarHostState: SnackbarHostState,
    key: NetworkKeyData,
    isSelected: Boolean,
    navigateToNetworkKey: (KeyIndex) -> Unit,
    onSwiped: (NetworkKeyData) -> Unit,
    onUndoClicked: (NetworkKeyData) -> Unit,
    remove: (NetworkKeyData) -> Unit,
) {
    // Hold the current state from the Swipe to Dismiss composable
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            handleValueChange(
                scope = scope,
                context = context,
                snackbarHostState = snackbarHostState,
                key = key
            )
        },
        positionalThreshold = { it * 0.5f }
    )
    SwipeDismissItem(
        dismissState = dismissState,
        content = {
            ElevatedCardItem(
                onClick = { navigateToNetworkKey(key.index) },
                colors = when (isSelected) {
                    true -> CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    else -> CardDefaults.outlinedCardColors()
                },
                imageVector = Icons.Outlined.VpnKey,
                title = key.name,
                subtitle = key.key.toHexString()
            )
        }
    )

    if (dismissState.isDismissed()) {
        LaunchedEffect(Unit) {
            scope.launch {
                delay(250)
                onSwiped(key)
            }
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.label_network_key_deleted),
                    actionLabel = context.getString(R.string.action_undo),
                    withDismissAction = true,
                    duration = SnackbarDuration.Short
                ).also {
                    when (it) {
                        SnackbarResult.Dismissed -> remove(key)
                        SnackbarResult.ActionPerformed -> {
                            dismissState.reset()
                            onUndoClicked(key)
                        }
                    }
                }
            }
        }
    }
}

private fun handleValueChange(
    scope: CoroutineScope,
    context: Context,
    snackbarHostState: SnackbarHostState,
    key: NetworkKeyData
) = when {
    key.isPrimary -> {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.error_cannot_delete_primary_network_key)
            )
        }
        false
    }

    key.isInUse -> {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.error_cannot_delete_key_in_use)
            )
        }
        false
    }

    else -> true
}