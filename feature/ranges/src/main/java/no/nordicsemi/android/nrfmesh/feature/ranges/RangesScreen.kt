package no.nordicsemi.android.nrfmesh.feature.ranges

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.GroupWork
import androidx.compose.material.icons.outlined.Lan
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarDuration
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.ui.AddressRangeLegendsForRanges
import no.nordicsemi.android.nrfmesh.core.ui.MeshAlertDialog
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.android.nrfmesh.core.ui.MeshOutlinedHexTextField
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.core.ui.SwipeDismissItem
import no.nordicsemi.android.nrfmesh.core.ui.isDismissed
import no.nordicsemi.kotlin.data.toByteArray
import no.nordicsemi.kotlin.data.toHexString
import no.nordicsemi.kotlin.mesh.core.model.GroupRange
import no.nordicsemi.kotlin.mesh.core.model.Range
import no.nordicsemi.kotlin.mesh.core.model.SceneRange
import no.nordicsemi.kotlin.mesh.core.model.UnicastRange
import no.nordicsemi.kotlin.mesh.core.model.overlaps

@OptIn(ExperimentalMaterial3Api::class, ExperimentalStdlibApi::class)
@Composable
fun RangesScreen(
    snackbarHostState: SnackbarHostState,
    title: String,
    ranges: List<Range>,
    otherRanges: List<Range>,
    isValidBound: (UShort) -> Boolean,
    addRange: (start: UShort, end: UShort) -> Unit,
    onRangeUpdated: (Range, UShort, UShort) -> Unit,
    onUndoClicked: (Range) -> Unit,
    onSwiped: (Range) -> Unit,
    remove: (Range) -> Unit,
    resolve: () -> Unit,
) {
    val context = LocalContext.current
    var showAddRangeDialog by remember { mutableStateOf(false) }
    var rangeToEdit by remember { mutableStateOf<Range?>(null) }
    Column {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            SectionTitle(title = title)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        rangeToEdit = null
                        showAddRangeDialog = true
                    }
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = stringResource(R.string.label_add_range)
                        )
                    }
                }
                Spacer(modifier = Modifier.size(8.dp))
                AnimatedVisibility(visible = ranges.overlaps(otherRanges)) {
                    OutlinedButton(
                        onClick = resolve,
                        border = BorderStroke(width = 1.dp, color = Color.Red),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoFixHigh,
                                contentDescription = null,
                                tint = Color.Red
                            )
                            Text(
                                modifier = Modifier.padding(start = 8.dp),
                                text = stringResource(R.string.label_resolve),
                                color = Color.Red
                            )
                        }
                    }
                }
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f, false),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (ranges.isNotEmpty()) {
                items(items = ranges) { range ->
                    // Hold the current state from the Swipe to Dismiss composable
                    val currentItem by rememberUpdatedState(newValue = range)
                    val dismissState = rememberSwipeToDismissBoxState(
                        positionalThreshold = { it * 0.5f }
                    )
                    SwipeDismissItem(
                        dismissState = dismissState,
                        content = {
                            OutlinedCard {
                                AllocatedRange(
                                    imageVector = range.toImageVector(),
                                    title = "0x${
                                        range.low.toByteArray().toHexString()
                                    } - 0x${range.high.toByteArray().toHexString()}",
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
            } else {
                item {
                    MeshNoItemsAvailable(
                        imageVector = Icons.Outlined.AutoAwesome,
                        title = stringResource(R.string.no_ranges_currently_added)
                    )
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp))
        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
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
            } ?: AddRangeDialog(
                isValidBound = isValidBound,
                onDismissRequest = { showAddRangeDialog = false },
                onConfirmClicked = { start, end -> addRange(start, end) }
            )
        }
    }
}

@Composable
private fun AddRangeDialog(
    isValidBound: (UShort) -> Boolean,
    onDismissRequest: () -> Unit,
    onConfirmClicked: (start: UShort, end: UShort) -> Unit,
) {
    var start by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var end by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    val scope = rememberCoroutineScope()
    var startInFocus by rememberSaveable { mutableStateOf(false) }
    var endInFocus by rememberSaveable { mutableStateOf(false) }
    var invalidStart by rememberSaveable { mutableStateOf(false) }
    var invalidEnd by rememberSaveable { mutableStateOf(false) }
    var supportingErrorText by rememberSaveable { mutableStateOf("") }
    MeshAlertDialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(usePlatformDefaultWidth = true),
        onConfirmClick = {
            val startValue = start.text.trim()
            val endValue = end.text.trim()
            if (startValue.isNotBlank() && endValue.isNotBlank()) {
                scope.launch {
                    onDismissRequest()
                }.invokeOnCompletion {
                    onConfirmClicked(startValue.toUShort(radix = 16), endValue.toUShort(radix = 16))
                }
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
    onConfirmClicked: (Range, UShort, UShort) -> Unit,
) {
    var start by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(range.low.toByteArray().toHexString()))
    }
    var end by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(range.high.toByteArray().toHexString()))
    }
    var startInFocus by rememberSaveable { mutableStateOf(false) }
    var endInFocus by rememberSaveable { mutableStateOf(false) }
    var invalidStart by rememberSaveable { mutableStateOf(false) }
    var invalidEnd by rememberSaveable { mutableStateOf(false) }
    var supportingErrorText by rememberSaveable { mutableStateOf("") }
    MeshAlertDialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(usePlatformDefaultWidth = true),
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
                    label = { Text(text = stringResource(R.string.label_lower_bound)) },
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
                    label = { Text(text = stringResource(R.string.label_lower_bound)) },
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
}