@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.feature.scenes

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissState
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.android.feature.scenes.R
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.android.nrfmesh.core.ui.MeshTwoLineListItem
import no.nordicsemi.android.nrfmesh.core.ui.SwipeDismissItem
import no.nordicsemi.android.nrfmesh.core.ui.isDismissed
import no.nordicsemi.android.nrfmesh.core.ui.showSnackbar
import no.nordicsemi.kotlin.mesh.core.exception.NoSceneRangeAllocated
import no.nordicsemi.kotlin.mesh.core.model.Scene
import no.nordicsemi.kotlin.mesh.core.model.SceneNumber
import no.nordicsemi.kotlin.mesh.core.model.toHex

@Composable
internal fun ScenesRoute(
    viewModel: ScenesViewModel = hiltViewModel(),
    navigateToScene: (SceneNumber) -> Unit
) {
    val uiState: ScenesScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    ScenesScreen(
        uiState = uiState,
        navigateToScene = navigateToScene,
        onAddSceneClicked = viewModel::addScene,
        onSwiped = viewModel::onSwiped,
        onUndoClicked = viewModel::onUndoSwipe,
        remove = viewModel::remove
    )
}

@Composable
private fun ScenesScreen(
    uiState: ScenesScreenUiState,
    navigateToScene: (SceneNumber) -> Unit,
    onAddSceneClicked: () -> Scene?,
    onSwiped: (Scene) -> Unit,
    onUndoClicked: (Scene) -> Unit,
    remove: (Scene) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        floatingActionButton = {
            // if (uiState.hasProvisioners) Enable this when we have support for adding provisioners
            ExtendedFloatingActionButton(onClick = {
                snackbarHostState.currentSnackbarData?.dismiss()
                addScene(
                    context = context,
                    scope = coroutineScope,
                    snackbarHostState = snackbarHostState,
                    onAddSceneClicked = onAddSceneClicked,
                    navigateToScene = navigateToScene
                )
            }) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(R.string.action_add_scene)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (uiState.scenes.isEmpty()) {
            true -> MeshNoItemsAvailable(
                imageVector = Icons.Outlined.AutoAwesome,
                title = stringResource(R.string.no_scenes_currently_added),
                rationale = stringResource(R.string.provisioner_rationale_for_scenes)
            )

            false -> Scenes(
                padding = padding,
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
}

@Composable
private fun Scenes(
    padding: PaddingValues,
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
        contentPadding = padding,
        modifier = Modifier.fillMaxSize(),
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
    val dismissState = rememberSwipeToDismissState(
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
                MeshTwoLineListItem(
                    modifier = Modifier.clickable {
                        navigateToScene(scene.number)
                    },
                    leadingComposable = {
                        Icon(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = LocalContentColor.current.copy(alpha = 0.6f)
                        )
                    },
                    title = scene.name,
                    subtitle = scene.number.toHex(prefix0x = true)
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