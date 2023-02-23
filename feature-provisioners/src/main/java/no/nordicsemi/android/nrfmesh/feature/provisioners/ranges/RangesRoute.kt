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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import no.nordicsemi.android.feature.provisioners.R
import no.nordicsemi.android.nrfmesh.core.ui.*
import no.nordicsemi.android.nrfmesh.feature.provisioners.AllocatedRange
import no.nordicsemi.kotlin.mesh.core.model.*
import java.util.*

@Composable
internal fun RangesRoute(
    viewModel: RangesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RangesScreen(
        uiState = uiState,
        addRange = viewModel::addRange,
        onRangeUpdated = viewModel::onRangeUpdated,
        onSwiped = viewModel::onSwiped,
        onUndoClicked = viewModel::onUndoSwipe,
        remove = viewModel::remove
    )
}

@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
private fun RangesScreen(
    uiState: RangesScreenUiState,
    addRange: (start: UInt, end: UInt) -> Result<Unit>,
    onRangeUpdated: (Range, Range) -> Unit,
    onSwiped: (Range) -> Unit,
    onUndoClicked: (Range) -> Unit,
    remove: (Range) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddRangeDialog by remember { mutableStateOf(false) }
    Scaffold(
        floatingActionButton = {
            if (!uiState.conflicts) {
                ExtendedFloatingActionButton(onClick = {
                    showAddRangeDialog = !showAddRangeDialog
                }) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = stringResource(R.string.action_add_range)
                    )
                }
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
                otherRanges = uiState.otherRanges,
                onRangeUpdated = onRangeUpdated,
                onSwiped = onSwiped,
                onUndoClicked = onUndoClicked,
                remove = remove
            )
        }
        if (showAddRangeDialog) {
            AddRangeDialog(
                scope = coroutineScope,
                snackbarHostState = snackbarHostState,
                onDismissRequest = { showAddRangeDialog = false },
                onConfirmClicked = { start, end -> addRange(start, end) }
            )
        }
    }
}

@Composable
private fun Ranges(
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    ranges: List<Range>,
    otherRanges: List<Range>,
    onRangeUpdated: (Range, Range) -> Unit,
    onSwiped: (Range) -> Unit,
    onUndoClicked: (Range) -> Unit,
    remove: (Range) -> Unit
) {
    val listState = rememberLazyListState()
    var showAddRangeDialog by remember { mutableStateOf(false) }
    var rangeToEdit by rememberSaveable { mutableStateOf<Range?>(null) }
    Column {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f, true),
            state = listState
        ) {
            items(items = ranges) { range ->
                val conflicts = otherRanges.filter { it.overlap(range) != null }
                // Hold the current state from the Swipe to Dismiss composable
                val dismissState = rememberDismissState()
                var rangeDismissed by remember { mutableStateOf(false) }
                if (rangeDismissed) {
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
                        rangeDismissed = isDismissed
                        Surface(color = MaterialTheme.colorScheme.background) {
                            AllocatedRange(
                                imageVector = range.toImageVector(),
                                title = "${range.low.toHex(true)} - ${range.high.toHex(true)}",
                                range = range,
                                otherRanges = conflicts,
                                onClick = {
                                    rangeToEdit = it
                                    showAddRangeDialog = true
                                }
                            )
                        }
                    }
                )
            }
        }
        Divider()
        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 8.dp),
            text = stringResource(R.string.label_overlapping_ranges_rationale),
            style = MaterialTheme.typography.labelMedium
        )
        AddressRangeLegendsForRanges()
        Spacer(modifier = Modifier.size(16.dp))

        if (showAddRangeDialog) {
            rangeToEdit?.let { range ->
                UpdateRangeDialog(
                    onDismissRequest = { showAddRangeDialog = false },
                    range = range,
                    onConfirmClicked = { newRange ->
                        onRangeUpdated(range, newRange)
                    }
                )
            }
        }
    }
}


@Composable
private fun AddRangeDialog(
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onDismissRequest: () -> Unit,
    onConfirmClicked: (start: UInt, end: UInt) -> Result<Unit>
) {
    val context = LocalContext.current
    var start by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var end by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var startInFocus by rememberSaveable { mutableStateOf(false) }
    var endInFocus by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf(false) }
    MeshAlertDialog(
        onDismissRequest = { onDismissRequest() },
        onConfirmClick = {
            val startValue = start.text.trim()
            val endValue = end.text.trim()
            if (startValue.isNotBlank() && endValue.isNotBlank()) {
                onConfirmClicked(startValue.toUInt(radix = 16), endValue.toUInt(radix = 16))
                    .onSuccess {
                        onDismissRequest()
                    }
                    .onFailure {
                        error = true
                        showSnackbar(
                            scope = scope,
                            snackbarHostState = snackbarHostState,
                            message = it.message ?: context.getString(R.string.unknown_error)
                        )
                    }
            }
        },
        onDismissClick = { onDismissRequest() },
        icon = Icons.Outlined.Compress,
        title = stringResource(R.string.title_new_range),
        content = {
            Column {
                Text(
                    text = stringResource(R.string.label_new_range_rationale)
                )
                Spacer(modifier = Modifier.size(16.dp))
                MeshOutlinedHexTextField(
                    modifier = Modifier.clickable {
                        startInFocus = true
                        endInFocus = false
                    },
                    showPrefix = true,
                    onFocus = startInFocus,
                    value = start,
                    onValueChanged = {
                        startInFocus = true
                        endInFocus = false
                        error = false
                        start = it
                    },
                    label = { Text(text = stringResource(R.string.label_lower_bound)) },
                    internalTrailingIcon = {
                        IconButton(
                            enabled = start.text.isNotBlank(),
                            onClick = {
                                startInFocus = true
                                endInFocus = false
                                error = false
                                start = TextFieldValue("")
                            }
                        ) { Icon(imageVector = Icons.Outlined.Clear, contentDescription = null) }
                    },
                    regex = Regex("[0-9A-Fa-f]{0,4}"),
                    isError = error
                )
                Spacer(modifier = Modifier.size(16.dp))
                MeshOutlinedHexTextField(
                    modifier = Modifier.clickable {
                        startInFocus = false
                        endInFocus = true
                    },
                    showPrefix = true,
                    onFocus = endInFocus,
                    value = end,
                    onValueChanged = {
                        startInFocus = false
                        endInFocus = true
                        error = false
                        end = it
                    },
                    label = { Text(text = stringResource(R.string.label_upper_bound)) },
                    internalTrailingIcon = {
                        IconButton(
                            enabled = end.text.isNotBlank(),
                            onClick = {
                                startInFocus = false
                                endInFocus = true
                                error = false
                                end = TextFieldValue("")
                            }
                        ) { Icon(imageVector = Icons.Outlined.Clear, contentDescription = null) }
                    },
                    regex = Regex("[0-9A-Fa-f]{0,4}")
                )
            }
        }
    )
}

@Composable
private fun UpdateRangeDialog(
    onDismissRequest: () -> Unit,
    range: Range,
    onConfirmClicked: (Range) -> Unit
) {
    var start by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(range.low.toHex()))
    }
    var end by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(range.high.toHex()))
    }
    var startInFocus by rememberSaveable { mutableStateOf(false) }
    var endInFocus by rememberSaveable { mutableStateOf(false) }
    MeshAlertDialog(
        onDismissRequest = { onDismissRequest() },
        onConfirmClick = {
            val startValue = start.text.trim()
            val endValue = end.text.trim()
            if (startValue.isNotBlank() && endValue.isNotBlank()) {
                onDismissRequest()
                /*onConfirmClicked(
                    when (range) {
                        is UnicastRange ->
                            UnicastAddress(
                                address = startValue.toInt(radix = 16)
                            )..UnicastAddress(
                                address = endValue.toInt(radix = 16)
                            )
                        is GroupRange ->
                            GroupAddress(startValue.toInt(radix = 16))..GroupAddress(
                                endValue.toInt(
                                    radix = 16
                                )
                            )
                        is SceneRange -> SceneRange(
                            startValue.toUShort(radix = 16),
                            endValue.toUShort(radix = 16)
                        )
                    }
                )*/
            }
        },
        onDismissClick = { onDismissRequest() },
        icon = Icons.Outlined.Compress,
        title = stringResource(R.string.title_new_range),
        content = {
            Column {
                Text(
                    text = stringResource(R.string.label_new_range_rationale)
                )
                Spacer(modifier = Modifier.size(16.dp))
                MeshOutlinedHexTextField(
                    modifier = Modifier.clickable {
                        startInFocus = true
                        endInFocus = false
                    },
                    showPrefix = true,
                    onFocus = startInFocus,
                    value = start,
                    onValueChanged = {
                        startInFocus = true
                        endInFocus = false
                        start = it
                    },
                    internalTrailingIcon = {
                        IconButton(
                            enabled = start.text.isNotBlank(),
                            onClick = {
                                startInFocus = true
                                endInFocus = false
                                start = TextFieldValue("")
                            }
                        ) { Icon(imageVector = Icons.Outlined.Clear, contentDescription = null) }
                    },
                    regex = Regex("[0-9A-Fa-f]{0,4}")
                )
                Spacer(modifier = Modifier.size(16.dp))
                MeshOutlinedHexTextField(
                    modifier = Modifier.clickable {
                        startInFocus = false
                        endInFocus = true
                    },
                    showPrefix = true,
                    onFocus = endInFocus,
                    value = end,
                    onValueChanged = {
                        startInFocus = false
                        endInFocus = true
                        end = it
                    },
                    internalTrailingIcon = {
                        IconButton(
                            enabled = end.text.isNotBlank(),
                            onClick = {
                                startInFocus = false
                                endInFocus = true
                                end = TextFieldValue("")
                            }
                        ) { Icon(imageVector = Icons.Outlined.Clear, contentDescription = null) }
                    },
                    regex = Regex("[0-9A-Fa-f]{0,4}")
                )
            }
        }
    )
}

@Composable
internal fun Range.toImageVector() = when (this) {
    is UnicastRange -> Icons.Outlined.Lan
    is GroupRange -> Icons.Outlined.GroupWork
    is SceneRange -> Icons.Outlined.AutoAwesome
}