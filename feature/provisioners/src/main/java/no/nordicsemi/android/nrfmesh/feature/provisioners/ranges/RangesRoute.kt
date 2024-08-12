@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.feature.provisioners.ranges

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.GroupWork
import androidx.compose.material.icons.outlined.Lan
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.nrfmesh.core.ui.AddressRangeLegendsForRanges
import no.nordicsemi.android.nrfmesh.core.ui.MeshAlertDialog
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.android.nrfmesh.core.ui.MeshOutlinedHexTextField
import no.nordicsemi.android.nrfmesh.core.ui.SwipeDismissItem
import no.nordicsemi.android.nrfmesh.core.ui.isDismissed
import no.nordicsemi.android.nrfmesh.feature.provisioners.AllocatedRange
import no.nordicsemi.android.nrfmesh.feature.provisioners.R
import no.nordicsemi.kotlin.mesh.core.model.GroupRange
import no.nordicsemi.kotlin.mesh.core.model.Range
import no.nordicsemi.kotlin.mesh.core.model.SceneRange
import no.nordicsemi.kotlin.mesh.core.model.UnicastRange

@Composable
internal fun RangesRoute(
    uiState: RangesScreenUiState,
    addRange: (start: UInt, end: UInt) -> Unit,
    onRangeUpdated: (Range, UShort, UShort) -> Unit,
    onSwiped: (Range) -> Unit,
    onUndoClicked: (Range) -> Unit,
    remove: (Range) -> Unit,
    resolve: () -> Unit,
    isValidBound: (UShort) -> Boolean
) {
    RangesScreen(
        uiState = uiState,
        addRange = addRange,
        onRangeUpdated = onRangeUpdated,
        onSwiped = onSwiped,
        onUndoClicked = onUndoClicked,
        remove = remove,
        resolve = resolve,
        isValidBound = isValidBound
    )
}

@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
private fun RangesScreen(
    uiState: RangesScreenUiState,
    addRange: (start: UInt, end: UInt) -> Unit,
    onRangeUpdated: (Range, UShort, UShort) -> Unit,
    onSwiped: (Range) -> Unit,
    onUndoClicked: (Range) -> Unit,
    remove: (Range) -> Unit,
    resolve: () -> Unit,
    isValidBound: (UShort) -> Boolean
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddRangeDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (!uiState.conflicts) {
                ExtendedFloatingActionButton(
                    onClick = { showAddRangeDialog = !showAddRangeDialog },
                    icon = { Icon(imageVector = Icons.Rounded.Add, contentDescription = null) },
                    text = {
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = stringResource(R.string.action_add_range)
                        )
                    }
                )
            } else {
                ExtendedFloatingActionButton(
                    onClick = { resolve() },
                    icon = { Icon(imageVector = Icons.Outlined.Check, contentDescription = null) },
                    text = {
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = stringResource(R.string.label_resolve)
                        )
                    },
                    containerColor = Color.Red
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
                snackbarHostState = snackbarHostState,
                ranges = uiState.ranges,
                otherRanges = uiState.otherRanges,
                onRangeUpdated = onRangeUpdated,
                onSwiped = onSwiped,
                onUndoClicked = onUndoClicked,
                isValidBound = isValidBound,
                remove = remove
            )
        }
        if (showAddRangeDialog) {
            AddRangeDialog(
                isValidBound = isValidBound,
                onDismissRequest = { showAddRangeDialog = false }
            ) { start, end -> addRange(start, end) }
        }
        // TODO Look into how to handle back button on the top app bar for
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalStdlibApi::class)
@Composable
private fun Ranges(
    snackbarHostState: SnackbarHostState,
    ranges: List<Range>,
    otherRanges: List<Range>,
    onRangeUpdated: (Range, UShort, UShort) -> Unit,
    onSwiped: (Range) -> Unit,
    onUndoClicked: (Range) -> Unit,
    isValidBound: (UShort) -> Boolean,
    remove: (Range) -> Unit
) {
    val context = LocalContext.current
    var showAddRangeDialog by remember { mutableStateOf(false) }
    var rangeToEdit by remember { mutableStateOf<Range?>(null) }
    Column {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f, true),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = ranges) { range ->
                // Hold the current state from the Swipe to Dismiss composable
                val currentItem by rememberUpdatedState(newValue = range)
                val dismissState = rememberSwipeToDismissBoxState(
                    positionalThreshold = { it * 0.5f }
                )
                SwipeDismissItem(
                    dismissState = dismissState,
                    content = {
                        ElevatedCard {
                            AllocatedRange(
                                imageVector = range.toImageVector(),
                                title = "0x${range.low.toHexString()} - 0x${range.high.toHexString()}",
                                range = range,
                                otherRanges = otherRanges.filter { it.overlap(range) != null },
                                onClick = {
                                    rangeToEdit = it
                                    showAddRangeDialog = true
                                }
                            )
                        }
                    }
                )
                if (dismissState.isDismissed()) {
                    LaunchedEffect(snackbarHostState) {
                        onSwiped(currentItem)
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.label_range_deleted),
                            actionLabel = context.getString(R.string.action_undo),
                            withDismissAction = true,
                            duration = SnackbarDuration.Long
                        ).also {
                            when (it) {
                                SnackbarResult.Dismissed -> remove(currentItem)
                                SnackbarResult.ActionPerformed -> {
                                    dismissState.reset()
                                    onUndoClicked(currentItem)
                                }
                            }
                        }
                    }
                }
            }
        }
        HorizontalDivider()
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
                    isValidBound = isValidBound,
                    onConfirmClicked = { oldRange, low, high ->
                        onRangeUpdated(oldRange, low, high)
                    }
                )
            }
        }
    }
}

@Composable
private fun AddRangeDialog(
    isValidBound: (UShort) -> Boolean,
    onDismissRequest: () -> Unit,
    onConfirmClicked: (start: UInt, end: UInt) -> Unit
) {
    var start by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var end by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var startInFocus by rememberSaveable { mutableStateOf(false) }
    var endInFocus by rememberSaveable { mutableStateOf(false) }
    var invalidStart by rememberSaveable { mutableStateOf(false) }
    var invalidEnd by rememberSaveable { mutableStateOf(false) }
    var supportingErrorText by rememberSaveable { mutableStateOf("") }
    MeshAlertDialog(
        onDismissRequest = { onDismissRequest() },
        onConfirmClick = {
            val startValue = start.text.trim()
            val endValue = end.text.trim()
            if (startValue.isNotBlank() && endValue.isNotBlank()) {
                onConfirmClicked(startValue.toUInt(radix = 16), endValue.toUInt(radix = 16))
                onDismissRequest()
            }
        },
        onDismissClick = { onDismissRequest() },
        icon = Icons.Outlined.SwapHoriz,
        title = stringResource(R.string.title_new_range),
        error = invalidStart ||
                invalidEnd ||
                start.text.trim().isBlank() ||
                end.text.trim().isBlank(),
        content = {
            Column {
                Text(text = stringResource(R.string.label_new_range_rationale))
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
                        if (start.text.trim().isNotEmpty()) {
                            runCatching {
                                invalidStart = !isValidBound(start.text.toUShort(radix = 16))
                            }.onFailure { throwable ->
                                supportingErrorText = throwable.message ?: ""
                                invalidStart = true
                            }
                        }
                    },
                    label = { Text(text = stringResource(R.string.label_lower_bound)) },
                    internalTrailingIcon = {
                        IconButton(
                            enabled = start.text.isNotBlank(),
                            onClick = {
                                start = TextFieldValue("")
                                startInFocus = true
                                endInFocus = false
                                invalidStart = false
                            }
                        ) { Icon(imageVector = Icons.Outlined.Clear, contentDescription = null) }
                    },
                    regex = Regex("[0-9A-Fa-f]{0,4}"),
                    isError = invalidStart,
                    supportingText = {
                        if (invalidStart)
                            Text(
                                text = supportingErrorText,
                                color = MaterialTheme.colorScheme.error
                            )
                    }
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
                        if (end.text.trim().isNotEmpty()) {
                            runCatching {
                                invalidEnd = !isValidBound(end.text.toUShort(radix = 16))
                            }.onFailure { throwable ->
                                supportingErrorText = throwable.message ?: ""
                                invalidEnd = true
                            }
                        }
                    },
                    label = { Text(text = stringResource(R.string.label_upper_bound)) },
                    internalTrailingIcon = {
                        IconButton(
                            enabled = end.text.isNotBlank(),
                            onClick = {
                                end = TextFieldValue("")
                                startInFocus = false
                                endInFocus = true
                                invalidEnd = false
                            }
                        ) { Icon(imageVector = Icons.Outlined.Clear, contentDescription = null) }
                    },
                    regex = Regex("[0-9A-Fa-f]{0,4}"),
                    isError = invalidEnd,
                    supportingText = {
                        if (invalidEnd)
                            Text(
                                text = supportingErrorText,
                                color = MaterialTheme.colorScheme.error
                            )
                    }
                )
            }
        }
    )
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
private fun UpdateRangeDialog(
    onDismissRequest: () -> Unit,
    range: Range,
    isValidBound: (UShort) -> Boolean,
    onConfirmClicked: (Range, UShort, UShort) -> Unit
) {
    var start by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(range.low.toHexString()))
    }
    var end by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(range.high.toHexString()))
    }
    var startInFocus by rememberSaveable { mutableStateOf(false) }
    var endInFocus by rememberSaveable { mutableStateOf(false) }
    var invalidStart by rememberSaveable { mutableStateOf(false) }
    var invalidEnd by rememberSaveable { mutableStateOf(false) }
    var supportingErrorText by rememberSaveable { mutableStateOf("") }
    MeshAlertDialog(
        onDismissRequest = { onDismissRequest() },
        onConfirmClick = {
            val startValue = start.text.trim()
            val endValue = end.text.trim()
            if (startValue.isNotBlank() && endValue.isNotBlank()) {
                onDismissRequest()
                onConfirmClicked(
                    range,
                    startValue.toUShort(radix = 16),
                    endValue.toUShort(radix = 16)
                )
            }
        },
        onDismissClick = { onDismissRequest() },
        icon = Icons.Outlined.SwapHoriz,
        title = stringResource(R.string.title_new_range),
        error = invalidStart || invalidEnd ||
                start.text
                    .trim()
                    .isBlank() ||
                end.text
                    .trim()
                    .isBlank(),
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
                        if (start.text.trim().isNotEmpty()) {
                            runCatching {
                                invalidStart = !isValidBound(start.text.toUShort(radix = 16))
                            }.onFailure { throwable ->
                                supportingErrorText = throwable.message ?: ""
                                invalidStart = true
                            }
                        }
                    },
                    internalTrailingIcon = {
                        IconButton(
                            enabled = start.text.isNotBlank(),
                            onClick = {
                                start = TextFieldValue("")
                                startInFocus = true
                                endInFocus = false
                                invalidStart = false
                            }
                        ) { Icon(imageVector = Icons.Outlined.Clear, contentDescription = null) }
                    },
                    regex = Regex("[0-9A-Fa-f]{0,4}"),
                    isError = invalidStart,
                    supportingText = {
                        if (invalidStart)
                            Text(
                                text = supportingErrorText,
                                color = MaterialTheme.colorScheme.error
                            )
                    }
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
                        if (end.text.trim().isNotEmpty()) {
                            runCatching {
                                invalidEnd = !isValidBound(end.text.toUShort(radix = 16))
                            }.onFailure { throwable ->
                                supportingErrorText = throwable.message ?: ""
                                invalidEnd = true
                            }
                        }
                    },
                    internalTrailingIcon = {
                        IconButton(
                            enabled = end.text.isNotBlank(),
                            onClick = {
                                end = TextFieldValue("")
                                startInFocus = false
                                endInFocus = true
                                invalidEnd = false
                            }
                        ) { Icon(imageVector = Icons.Outlined.Clear, contentDescription = null) }
                    },
                    regex = Regex("[0-9A-Fa-f]{0,4}"),
                    isError = invalidEnd,
                    supportingText = {
                        if (invalidEnd)
                            Text(
                                text = supportingErrorText,
                                color = MaterialTheme.colorScheme.error
                            )
                    }
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
    else -> Icons.Outlined.Lan
}