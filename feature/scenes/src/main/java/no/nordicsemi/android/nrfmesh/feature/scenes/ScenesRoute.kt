@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.feature.scenes

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
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
import androidx.compose.material3.Surface
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
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.models.SceneData
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.core.ui.SwipeDismissItem
import no.nordicsemi.android.nrfmesh.core.ui.isDismissed
import no.nordicsemi.android.nrfmesh.core.ui.showSnackbar
import no.nordicsemi.kotlin.mesh.core.exception.NoSceneRangeAllocated
import no.nordicsemi.kotlin.mesh.core.model.Scene
import no.nordicsemi.kotlin.mesh.core.model.SceneNumber

@Composable
internal fun ScenesRoute(
    highlightSelectedItem: Boolean,
    scenes: List<SceneData>,
    onAddSceneClicked: () -> Scene?,
    navigateToScene: (SceneNumber) -> Unit,
    onSwiped: (SceneData) -> Unit,
    onUndoClicked: (SceneData) -> Unit,
    remove: (SceneData) -> Unit
) {
    Scenes(
        highlightSelectedItem = highlightSelectedItem,
        scenes = scenes,
        onAddSceneClicked = onAddSceneClicked,
        navigateToScene = navigateToScene,
        onSwiped = onSwiped,
        onUndoClicked = onUndoClicked,
        remove = remove
    )
}

@Composable
private fun Scenes(
    highlightSelectedItem: Boolean,
    scenes: List<SceneData>,
    onAddSceneClicked: () -> Scene?,
    navigateToScene: (SceneNumber) -> Unit,
    onSwiped: (SceneData) -> Unit,
    onUndoClicked: (SceneData) -> Unit,
    remove: (SceneData) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedSceneNumber by rememberSaveable { mutableStateOf<Int?>(null) }
    Scaffold(
        modifier = Modifier.background(color = Color.Red),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier.defaultMinSize(minWidth = 150.dp),
                text = { Text(text = stringResource(R.string.label_add_scene)) },
                icon = { Icon(imageVector = Icons.Outlined.Add, contentDescription = null) },
                onClick = {
                    runCatching {
                        onAddSceneClicked()
                    }.onSuccess { scene ->
                        scene?.number?.let {
                            selectedSceneNumber = it.toInt()
                            navigateToScene(it)
                        }
                    }.onFailure {
                        showSnackbar(
                            scope = scope,
                            snackbarHostState = snackbarHostState,
                            message = when (it) {
                                is NoSceneRangeAllocated -> it.message ?: context.getString(
                                    R.string.error_allocate_scene_range_to_provisioner
                                )

                                else -> it.message ?: context.getString(R.string.unknown_error)
                            }
                        )
                    }
                },
                expanded = true
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(paddingValues = paddingValues)
        ) {
            SectionTitle(
                modifier = Modifier.padding(top = 8.dp),
                title = stringResource(id = R.string.label_scenes)
            )
            when (scenes.isEmpty()) {
                true -> MeshNoItemsAvailable(
                    modifier = Modifier.fillMaxSize(),
                    imageVector = Icons.Outlined.AutoAwesome,
                    title = stringResource(R.string.no_scenes_currently_added)
                )

                false -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = scenes, key = { it.hashCode() }) { scene ->
                        val isSelected = highlightSelectedItem && scene.number.toInt() == selectedSceneNumber
                        SwipeToDismissScene(
                            context = context,
                            snackbarHostState = snackbarHostState,
                            scene = scene,
                            isSelected = isSelected,
                            navigateToScene = {
                                selectedSceneNumber = it.toInt()
                                navigateToScene(it)
                            },
                            onSwiped = {
                                onSwiped(it)
                                remove(it)
                            },
                            onUndoClicked = onUndoClicked,
                            remove = remove
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
private fun SwipeToDismissScene(
    context: Context,
    snackbarHostState: SnackbarHostState,
    scene: SceneData,
    isSelected: Boolean,
    navigateToScene: (SceneNumber) -> Unit,
    onSwiped: (SceneData) -> Unit,
    onUndoClicked: (SceneData) -> Unit,
    remove: (SceneData) -> Unit
) {
    val scope = rememberCoroutineScope()
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            handleValueChange(
                scope = scope,
                context = context,
                snackbarHostState = snackbarHostState,
                scene = scene
            )
        },
        positionalThreshold = { it * 0.5f }
    )
    SwipeDismissItem(
        dismissState = dismissState,
        content = {
            Surface(color = MaterialTheme.colorScheme.background) {
                ElevatedCardItem(
                    onClick = { navigateToScene(scene.number) },
                    colors = when (isSelected) {
                        true -> CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        else -> CardDefaults.outlinedCardColors()
                    },
                    imageVector = Icons.Outlined.AutoAwesome,
                    title = scene.name,
                    subtitle = "0x${scene.number.toHexString()}"
                )
            }
        }
    )
    if (dismissState.isDismissed()) {
        LaunchedEffect(key1 = snackbarHostState) {
            onSwiped(scene)
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.label_scene_deleted),
                actionLabel = context.getString(R.string.action_undo),
                withDismissAction = true,
                duration = SnackbarDuration.Short
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

private fun handleValueChange(
    scope: CoroutineScope,
    context: Context,
    snackbarHostState: SnackbarHostState,
    scene: SceneData
): Boolean = when {
    scene.isInUse -> {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.error_cannot_delete_scene_in_use)
            )
        }
        false
    }

    else -> true
}