@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class,
    ExperimentalLifecycleComposeApi::class
)

package no.nordicsemi.android.feature.scenes

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.ui.MeshLargeTopAppBar
import no.nordicsemi.android.nrfmesh.core.ui.MeshTwoLineListItem
import no.nordicsemi.android.nrfmesh.core.ui.SwipeDismissItem
import no.nordicsemi.android.nrfmesh.core.ui.showSnackbar
import no.nordicsemi.kotlin.mesh.core.exception.NoSceneRangeAllocated
import no.nordicsemi.kotlin.mesh.core.model.Scene
import no.nordicsemi.kotlin.mesh.core.model.SceneNumber
import no.nordicsemi.kotlin.mesh.core.model.toHex

@Composable
fun ScenesRoute(
    viewModel: ScenesViewModel = hiltViewModel(),
    navigateToScene: (SceneNumber) -> Unit,
    onBackClicked: () -> Unit
) {
    val uiState: ScenesScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    ScenesScreen(
        uiState = uiState,
        navigateToScene = navigateToScene,
        onAddSceneClicked = viewModel::addScene,
        onSwiped = viewModel::onSwiped,
        onUndoClicked = viewModel::onUndoSwipe,
        remove = viewModel::remove,
        onBackPressed = {
            viewModel.removeScenes()
            onBackClicked()
        }
    )
}

@Composable
private fun ScenesScreen(
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
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(connection = scrollBehavior.nestedScrollConnection),
        topBar = {
            MeshLargeTopAppBar(
                title = stringResource(id = R.string.label_scenes),
                navigationIcon = {
                    IconButton(onClick = {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        onBackPressed()
                    }) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = {
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
            true -> NoScenes()
            false -> Scenes(
                padding = padding,
                context = context,
                coroutineScope = coroutineScope,
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
    coroutineScope: CoroutineScope,
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
        items(items = scenes, key = { it.number.toInt() }) { scene ->
            // Hold the current state from the Swipe to Dismiss composable
            val dismissState = rememberDismissState {
                val state = it == DismissValue.DismissedToStart && scene.isInUse
                if (state) {
                    showSnackbar(
                        scope = coroutineScope,
                        snackbarHostState = snackbarHostState,
                        message = context.getString(R.string.error_cannot_delete_scene_in_use),
                        withDismissAction = true
                    )
                }
                !state
            }
            var keyDismissed by remember { mutableStateOf(false) }
            if (keyDismissed) {
                showSnackbar(
                    scope = coroutineScope,
                    snackbarHostState = snackbarHostState,
                    message = stringResource(R.string.label_scene_deleted),
                    actionLabel = stringResource(R.string.action_undo),
                    onDismissed = { remove(scene) },
                    onActionPerformed = {
                        onUndoClicked(scene)
                        coroutineScope.launch {
                            dismissState.reset()
                        }
                    },
                    withDismissAction = true
                )
            }
            SwipeDismissItem(
                dismissState = dismissState,
                background = { offsetX ->
                    val color = if (offsetX < (-30).dp) Color.Red else Color.DarkGray
                    val scale by animateFloatAsState(if (offsetX < (-50).dp) 1f else 0.75f)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color)
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            modifier = Modifier.scale(scale),
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "null"
                        )
                    }
                },
                content = { isDismissed ->
                    if (isDismissed) {
                        onSwiped(scene)
                    }
                    keyDismissed = isDismissed
                    Surface(color = MaterialTheme.colorScheme.background) {
                        MeshTwoLineListItem(
                            modifier = Modifier.clickable {
                                navigateToScene(scene.number)
                            },
                            leadingIcon = {
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
        }
    }
}

@Composable
private fun NoScenes() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            modifier = Modifier.size(128.dp),
            imageVector = Icons.Outlined.AutoAwesome,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.surfaceTint
        )
        Text(
            modifier = Modifier.padding(vertical = 16.dp),
            text = stringResource(R.string.no_scenes_currently_added)
        )
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