@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.feature.application.keys

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.models.ApplicationKeyData
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.core.ui.SwipeDismissItem
import no.nordicsemi.android.nrfmesh.core.ui.isDismissed
import no.nordicsemi.kotlin.data.toHexString
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex

@Composable
internal fun ApplicationKeysRoute(
    highlightSelectedItem: Boolean,
    keys: List<ApplicationKeyData>,
    onAddKeyClicked: () -> ApplicationKey,
    onApplicationKeyClicked: (KeyIndex) -> Unit,
    navigateToKey: (KeyIndex) -> Unit,
    onSwiped: (ApplicationKeyData) -> Unit,
    onUndoClicked: (ApplicationKeyData) -> Unit,
    remove: (ApplicationKeyData) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedKeyIndex by remember { mutableStateOf<KeyIndex?>(null) }
    Scaffold(
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
                        selectedKeyIndex = it.index
                        navigateToKey(it.index)
                    }
                },
                expanded = true
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
        ) {
            SectionTitle(title = stringResource(id = R.string.label_application_keys))
            when (keys.isEmpty()) {
                true -> MeshNoItemsAvailable(
                    modifier = Modifier.fillMaxSize(),
                    imageVector = Icons.Outlined.VpnKey,
                    title = stringResource(R.string.no_application_keys_available),
                )

                false -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(space = 8.dp)
                ) {
                    items(items = keys, key = { it.key.hashCode() }) { key ->
                        val isSelected = highlightSelectedItem && key.index == selectedKeyIndex
                        SwipeToDismissKey(
                            scope = scope,
                            context = context,
                            snackbarHostState = snackbarHostState,
                            key = key,
                            isSelected = isSelected,
                            onApplicationKeyClicked = {
                                selectedKeyIndex = it
                                onApplicationKeyClicked(it)
                            },
                            onSwiped = onSwiped,
                            onUndoClicked = onUndoClicked,
                            remove = remove
                        )
                    }
                }
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
    key: ApplicationKeyData,
    isSelected: Boolean,
    onApplicationKeyClicked: (KeyIndex) -> Unit,
    onSwiped: (ApplicationKeyData) -> Unit,
    onUndoClicked: (ApplicationKeyData) -> Unit,
    remove: (ApplicationKeyData) -> Unit,
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
                onClick = { onApplicationKeyClicked(key.index) },
                colors = when (isSelected) {
                    true -> CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    else -> CardDefaults.outlinedCardColors()
                },
                imageVector = Icons.Outlined.VpnKey,
                title = key.name,
                subtitle = "Bound to ${key.boundNetworkKey?.name ?: key.key.toHexString()}"
            )
        }
    )

    if (dismissState.isDismissed()) {
        LaunchedEffect(snackbarHostState) {
            scope.launch {
                delay(250)
                onSwiped(key)
            }
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.label_application_key_deleted),
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
    key: ApplicationKeyData,
) = when {
    // First we check if the key is in use
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