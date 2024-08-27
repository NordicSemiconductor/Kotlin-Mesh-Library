@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.feature.application.keys

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.android.nrfmesh.core.ui.SwipeDismissItem
import no.nordicsemi.android.nrfmesh.core.ui.isDismissed
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeysScreen
import no.nordicsemi.kotlin.data.toHexString
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex

@Composable
internal fun ApplicationKeysRoute(
    appState: AppState,
    uiState: ApplicationKeysScreenUiState,
    navigateToKey: (KeyIndex) -> Unit,
    onAddKeyClicked: () -> ApplicationKey,
    onSwiped: (ApplicationKey) -> Unit,
    onUndoClicked: (ApplicationKey) -> Unit,
    remove: (ApplicationKey) -> Unit,
    onBackPressed: () -> Unit
) {
    val screen = appState.currentScreen as? ApplicationKeysScreen
    LaunchedEffect(key1 = screen) {
        screen?.buttons?.onEach { button ->
            when (button) {
                ApplicationKeysScreen.Actions.ADD_KEY -> navigateToKey(onAddKeyClicked().index)
                ApplicationKeysScreen.Actions.BACK -> onBackPressed()
            }
        }?.launchIn(this)
    }
    ApplicationsKeysScreen(
        uiState = uiState,
        navigateToApplicationKey = navigateToKey,
        onSwiped = onSwiped,
        onUndoClicked = onUndoClicked,
        remove = remove
    )
}

@Composable
private fun ApplicationsKeysScreen(
    uiState: ApplicationKeysScreenUiState,
    navigateToApplicationKey: (KeyIndex) -> Unit,
    onSwiped: (ApplicationKey) -> Unit,
    onUndoClicked: (ApplicationKey) -> Unit,
    remove: (ApplicationKey) -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    when (uiState.keys.isEmpty()) {
        true -> MeshNoItemsAvailable(
            imageVector = Icons.Outlined.VpnKey,
            title = stringResource(R.string.label_no_keys_added)
        )

        false -> ApplicationKeys(
            context = context,
            listState = listState,
            snackbarHostState = snackbarHostState,
            keys = uiState.keys,
            navigateToApplicationKey = navigateToApplicationKey,
            onSwiped = onSwiped,
            onUndoClicked = onUndoClicked,
            remove = remove
        )
    }
}

@Composable
private fun ApplicationKeys(
    context: Context,
    listState: LazyListState,
    snackbarHostState: SnackbarHostState,
    keys: List<ApplicationKey>,
    navigateToApplicationKey: (KeyIndex) -> Unit,
    onSwiped: (ApplicationKey) -> Unit,
    onUndoClicked: (ApplicationKey) -> Unit,
    remove: (ApplicationKey) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        items(items = keys, key = { it.key.hashCode() }) { key ->
            SwipeToDismissKey(
                key = key,
                context = context,
                snackbarHostState = snackbarHostState,
                navigateToApplicationKey = navigateToApplicationKey,
                onSwiped = onSwiped,
                onUndoClicked = onUndoClicked,
                remove = remove
            )
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
private fun SwipeToDismissKey(
    key: ApplicationKey,
    context: Context,
    snackbarHostState: SnackbarHostState,
    navigateToApplicationKey: (KeyIndex) -> Unit,
    onSwiped: (ApplicationKey) -> Unit,
    onUndoClicked: (ApplicationKey) -> Unit,
    remove: (ApplicationKey) -> Unit
) {
    // Hold the current state from the Swipe to Dismiss composable
    var shouldNotDismiss by remember { mutableStateOf(true) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            shouldNotDismiss = !key.isInUse
            shouldNotDismiss
        },
        positionalThreshold = { it * 0.5f }
    )
    SwipeDismissItem(
        dismissState = dismissState,
        content = {
            ElevatedCardItem(
                modifier = Modifier
                    .clickable { navigateToApplicationKey(key.index) },
                imageVector = Icons.Outlined.VpnKey,
                title = key.name,
                subtitle = key.key.toHexString()
            )
        }
    )

    if (!shouldNotDismiss) {
        LaunchedEffect(snackbarHostState) {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.error_cannot_delete_key_in_use),
                withDismissAction = true,
                duration = SnackbarDuration.Short,
            )
        }
    }
    if (dismissState.isDismissed()) {
        LaunchedEffect(snackbarHostState) {
            onSwiped(key)
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.label_application_key_deleted),
                actionLabel = context.getString(R.string.action_undo),
                withDismissAction = true,
                duration = SnackbarDuration.Long,
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