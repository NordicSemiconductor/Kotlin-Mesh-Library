@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.feature.scenes

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
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
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.ScenesScreen
import no.nordicsemi.kotlin.mesh.core.exception.NoSceneRangeAllocated
import no.nordicsemi.kotlin.mesh.core.model.Scene
import no.nordicsemi.kotlin.mesh.core.model.SceneNumber

@Composable
internal fun ScenesRoute(
    appState: AppState,
    uiState: ScenesScreenUiState,
    navigateToScene: (SceneNumber) -> Unit,
    onAddSceneClicked: () -> Scene?,
    onSwiped: (Scene) -> Unit,
    onUndoClicked: (Scene) -> Unit,
    remove: (Scene) -> Unit,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val screen = appState.currentScreen as? ScenesScreen
    LaunchedEffect(key1 = screen) {
        screen?.buttons?.onEach { button ->
            when (button) {
                ScenesScreen.Actions.ADD_SCENE -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    addScene(
                        context = context,
                        scope = coroutineScope,
                        snackbarHostState = snackbarHostState,
                        onAddSceneClicked = onAddSceneClicked,
                        navigateToScene = navigateToScene
                    )
                }
                ScenesScreen.Actions.BACK -> onBackPressed()
            }

        }?.launchIn(this)
    }
    ScenesScreen(
        context = context,
        snackbarHostState = snackbarHostState,
        uiState = uiState,
        navigateToScene = navigateToScene,
        onSwiped = onSwiped,
        onUndoClicked = onUndoClicked,
        remove = remove
    )
}

@Composable
private fun ScenesScreen(
    context: Context,
    snackbarHostState: SnackbarHostState,
    uiState: ScenesScreenUiState,
    navigateToScene: (SceneNumber) -> Unit,
    onSwiped: (Scene) -> Unit,
    onUndoClicked: (Scene) -> Unit,
    remove: (Scene) -> Unit
) {

    when (uiState.scenes.isEmpty()) {
        true -> MeshNoItemsAvailable(
            imageVector = Icons.Outlined.AutoAwesome,
            title = stringResource(R.string.no_scenes_currently_added),
            rationale = stringResource(R.string.provisioner_rationale_for_scenes)
        )

        false -> Scenes(
            context = context,
            snackbarHostState = snackbarHostState,
            scenes = uiState.scenes,
            navigateToScene = navigateToScene,
            onSwiped = onSwiped,
            onUndoClicked = onUndoClicked,
            remove = remove
        )
    }
}

@Composable
private fun Scenes(
    context: Context,
    snackbarHostState: SnackbarHostState,
    scenes: List<Scene>,
    navigateToScene: (SceneNumber) -> Unit,
    onSwiped: (Scene) -> Unit,
    onUndoClicked: (Scene) -> Unit,
    remove: (Scene) -> Unit
) {
    val listState = rememberLazyListState()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        state = listState
    ) {
        items(items = scenes, key = { it.hashCode() }) { scene ->
            SwipeToDismissScene(
                scene = scene,
                context = context,
                snackbarHostState = snackbarHostState,
                navigateToScene = navigateToScene,
                onSwiped = onSwiped,
                onUndoClicked = onUndoClicked,
                remove = remove
            )
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
private fun SwipeToDismissScene(
    scene: Scene,
    context: Context,
    snackbarHostState: SnackbarHostState,
    navigateToScene: (SceneNumber) -> Unit,
    onSwiped: (Scene) -> Unit,
    onUndoClicked: (Scene) -> Unit,
    remove: (Scene) -> Unit
) {
    // Hold the current state from the Swipe to Dismiss composable
    var shouldNotDismiss by remember {
        mutableStateOf(true)
    }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            shouldNotDismiss = !scene.isInUse
            shouldNotDismiss
        },
        positionalThreshold = { it * 0.5f }
    )
    SwipeDismissItem(
        dismissState = dismissState,
        content = {
            Surface(color = MaterialTheme.colorScheme.background) {
                ElevatedCardItem(
                    onClick = { navigateToScene(scene.number) },
                    imageVector = Icons.Outlined.AutoAwesome,
                    title = scene.name,
                    subtitle = "0x${scene.number.toHexString()}"
                )
            }
        }
    )
    if (!shouldNotDismiss) {
        LaunchedEffect(key1 = snackbarHostState) {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.error_scene_in_use),
                withDismissAction = true,
                duration = SnackbarDuration.Short,
            )
        }
    }
    if (dismissState.isDismissed()) {
        LaunchedEffect(key1 = snackbarHostState) {
            onSwiped(scene)
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.label_scene_deleted),
                actionLabel = context.getString(R.string.action_undo),
                withDismissAction = true,
                duration = SnackbarDuration.Long,
            ).also {
                when (it) {
                    SnackbarResult.Dismissed -> remove(scene)
                    SnackbarResult.ActionPerformed -> {
                        dismissState.reset()
                        onUndoClicked(scene)
                    }
                }
            }
        }
    }
}

private fun addScene(
    context: Context,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onAddSceneClicked: () -> Scene?,
    navigateToScene: (SceneNumber) -> Unit
) {
    runCatching {
        onAddSceneClicked()?.let {
            navigateToScene(it.number)
        } ?: run {
            showSnackbar(
                scope = scope,
                snackbarHostState = snackbarHostState,
                message = context.getString(
                    R.string.error_no_allocated_scene_numbers_available
                )
            )
        }
    }.getOrElse {
        showSnackbar(
            scope = scope,
            snackbarHostState = snackbarHostState,
            message = when (it) {
                is NoSceneRangeAllocated -> it.message ?: context.getString(
                    R.string.error_allocate_scene_range_to_provisioner
                )

                else -> it.message ?: context.getString(
                    R.string.unknown_error
                )
            }
        )
    }
}