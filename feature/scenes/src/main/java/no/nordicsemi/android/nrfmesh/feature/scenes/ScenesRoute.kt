@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.feature.scenes

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import no.nordicsemi.kotlin.mesh.core.exception.NoSceneRangeAllocated
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.Scene
import no.nordicsemi.kotlin.mesh.core.model.SceneNumber

@Composable
internal fun ScenesRoute(
    snackbarHostState: SnackbarHostState,
    highlightSelectedItem: Boolean,
    selectedSceneNumber: SceneNumber?,
    scenes: List<SceneData>,
    onAddSceneClicked: () -> Scene,
    onSceneClicked: (KeyIndex) -> Unit,
    navigateToScene: (SceneNumber) -> Unit,
    onSwiped: (SceneData) -> Unit,
    onUndoClicked: (SceneData) -> Unit,
    remove: (SceneData) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    Scaffold(
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
                        navigateToScene(scene.number)
                    }.onFailure {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = when (it) {
                                    is NoSceneRangeAllocated -> it.message ?: context.getString(
                                        R.string.error_allocate_scene_range_to_provisioner
                                    )

                                    else -> it.message ?: context.getString(R.string.unknown_error)
                                }
                            )
                        }
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
            when (scenes.isEmpty()) {
                true -> MeshNoItemsAvailable(
                    modifier = Modifier.fillMaxSize(),
                    imageVector = Icons.Outlined.AutoAwesome,
                    title = stringResource(R.string.no_scenes_currently_added)
                )

                false -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    // Removed in favor of padding in SwipeToDismissKey so that hiding an item will not leave any gaps
                    //verticalArrangement = Arrangement.spacedBy(space = 8.dp)
                ) {
                    item {
                        SectionTitle(
                            modifier = Modifier.padding(vertical = 8.dp),
                            title = stringResource(id = R.string.label_scenes)
                        )
                    }
                    items(items = scenes, key = { it.id }) { scene ->
                        val isSelected =
                            highlightSelectedItem && scene.number == selectedSceneNumber
                        var visibility by remember { mutableStateOf(true) }
                        AnimatedVisibility(visible = visibility) {
                            SwipeToDismissScene(
                                scope = scope,
                                context = context,
                                snackbarHostState = snackbarHostState,
                                scene = scene,
                                isSelected = isSelected,
                                onSceneClicked = onSceneClicked,
                                onSwiped = {
                                    visibility = false
                                    onSwiped(it)
                                },
                                onUndoClicked = {
                                    visibility = true
                                    onUndoClicked(it)
                                },
                                remove = remove
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
private fun SwipeToDismissScene(
    scope: CoroutineScope,
    context: Context,
    snackbarHostState: SnackbarHostState,
    scene: SceneData,
    isSelected: Boolean,
    onSceneClicked: (SceneNumber) -> Unit,
    onSwiped: (SceneData) -> Unit,
    onUndoClicked: (SceneData) -> Unit,
    remove: (SceneData) -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState()
    SwipeToDismissBox(
        // Added instead of using Arrangement.spacedBy to avoid leaving gaps when an item is swiped away.
        modifier = Modifier.padding(bottom = 8.dp),
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled,
                    SwipeToDismissBoxValue.StartToEnd,
                    SwipeToDismissBoxValue.EndToStart,
                        -> if (scene.isInUse) Color.Gray else Color.Red
                }
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = color, shape = CardDefaults.elevatedShape)
                    .padding(horizontal = 16.dp),
                contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd)
                    Alignment.CenterStart
                else Alignment.CenterEnd
            ) {
                Icon(imageVector = Icons.Outlined.Delete, contentDescription = "null")
            }
        },
        onDismiss = {
            snackbarHostState.currentSnackbarData?.dismiss()
            if (scene.isInUse) {
                // The following functions are invoked in their own coroutine to ensure
                // that they are executed sequentially
                scope.launch {
                    dismissState.reset()
                    snackbarHostState.showSnackbar(
                        message = context.getString(
                            R.string.label_cannot_delete_scene_in_use,
                            scene.name
                        )
                    )
                }
            } else {
                onSwiped(scene)
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(
                            R.string.label_scene_deleted,
                            scene.name
                        ),
                        actionLabel = context.getString(R.string.action_undo),
                        withDismissAction = true,
                        duration = SnackbarDuration.Short
                    )

                    when (result) {
                        SnackbarResult.ActionPerformed -> {
                            onUndoClicked(scene)
                            dismissState.reset()
                        }

                        SnackbarResult.Dismissed -> remove(scene)
                    }
                }
            }
        },
        content = {
            ElevatedCardItem(
                onClick = { onSceneClicked(scene.number) },
                colors = when (isSelected) {
                    true -> CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    else -> CardDefaults.outlinedCardColors()
                },
                imageVector = Icons.Outlined.AutoAwesome,
                title = scene.name,
                subtitle = "Scene number: ${
                    scene.number.toHexString(
                        format = HexFormat {
                            number.prefix = "0x"
                            upperCase = true
                        }
                    )
                }"
            )
        }
    )
}