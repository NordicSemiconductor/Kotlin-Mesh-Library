@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)

package no.nordicsemi.android.nrfmesh.feature.provisioners.ranges

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import no.nordicsemi.android.feature.provisioners.R
import no.nordicsemi.android.nrfmesh.core.ui.*
import no.nordicsemi.kotlin.mesh.core.model.*
import java.util.*

@Composable
internal fun RangesRoute(
    viewModel: RangesViewModel = hiltViewModel()
) {
    val uiState: RangesScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    RangesScreen(
        uiState = uiState,
        onSwiped = viewModel::onSwiped,
        onUndoClicked = viewModel::onUndoSwipe,
        remove = viewModel::remove
    )
}

@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
private fun RangesScreen(
    uiState: RangesScreenUiState,
    onSwiped: (Range) -> Unit,
    onUndoClicked: (Range) -> Unit,
    remove: (Range) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = {

            }) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(R.string.action_add_range)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        when (uiState.ranges.isEmpty()) {
            true -> MeshNoItemsAvailable(
                imageVector = Icons.Outlined.AutoAwesome,
                title = stringResource(R.string.no_ranges_currently_added)
            )
            false -> Ranges(
                coroutineScope = coroutineScope,
                snackbarHostState = snackbarHostState,
                ranges = uiState.ranges,
                onSwiped = onSwiped,
                onUndoClicked = onUndoClicked,
                remove = remove
            )
        }

    }
}

@Composable
private fun Ranges(
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    ranges: List<Range>,
    onSwiped: (Range) -> Unit,
    onUndoClicked: (Range) -> Unit,
    remove: (Range) -> Unit
) {
    val listState = rememberLazyListState()
    Column {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f, true),
            state = listState
        ) {
            items(items = ranges) { range ->
                // Hold the current state from the Swipe to Dismiss composable
                val dismissState = rememberDismissState()
                var keyDismissed by remember { mutableStateOf(false) }
                if (keyDismissed) {
                    showSnackbar(
                        scope = coroutineScope,
                        snackbarHostState = snackbarHostState,
                        message = stringResource(R.string.label_range_deleted),
                        actionLabel = stringResource(R.string.action_undo),
                        onDismissed = { remove(range) },
                        onActionPerformed = {
                            onUndoClicked(range)
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
                            onSwiped(range)
                        }
                        keyDismissed = isDismissed
                        Surface(color = MaterialTheme.colorScheme.background) {
                            MeshTwoLineListItem(
                                modifier = Modifier.clickable {},
                                leadingComposable = {
                                    Icon(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        imageVector = when (range) {
                                            is UnicastRange -> Icons.Outlined.Lan
                                            is GroupRange -> Icons.Outlined.GroupWork
                                            is SceneRange -> Icons.Outlined.AutoAwesome
                                        },
                                        contentDescription = null,
                                        tint = LocalContentColor.current.copy(alpha = 0.6f)
                                    )
                                },
                                title = "${range.low.toHex(true)} - ${range.high.toHex(true)}",
                                trailingComposable = {
                                    Icon(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = null,
                                        tint = LocalContentColor.current.copy(alpha = 0.6f)
                                    )
                                }
                            )
                        }
                    }
                )
            }

            item {
            }
        }
        AddressRangeLegendsForRanges()
        Spacer(modifier = Modifier.size(16.dp))
    }
}