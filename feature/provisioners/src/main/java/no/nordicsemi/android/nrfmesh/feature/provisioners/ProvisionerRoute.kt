package no.nordicsemi.android.nrfmesh.feature.provisioners

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GppMaybe
import androidx.compose.material.icons.outlined.GroupWork
import androidx.compose.material.icons.outlined.Lan
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.common.convertToString
import no.nordicsemi.android.nrfmesh.core.data.models.ProvisionerData
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.ui.AddressRangeLegendsForProvisioner
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItemTextField
import no.nordicsemi.android.nrfmesh.core.ui.MeshAlertDialog
import no.nordicsemi.android.nrfmesh.core.ui.MeshOutlinedTextField
import no.nordicsemi.android.nrfmesh.core.ui.MeshTwoLineListItem
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.feature.ranges.AllocatedRanges
import no.nordicsemi.android.nrfmesh.feature.ranges.RangesScreen
import no.nordicsemi.kotlin.mesh.core.model.GroupAddress
import no.nordicsemi.kotlin.mesh.core.model.GroupRange
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import no.nordicsemi.kotlin.mesh.core.model.Range
import no.nordicsemi.kotlin.mesh.core.model.Scene
import no.nordicsemi.kotlin.mesh.core.model.SceneRange
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.UnicastRange
import no.nordicsemi.kotlin.mesh.core.model.minus
import no.nordicsemi.kotlin.mesh.core.model.overlaps
import no.nordicsemi.kotlin.mesh.core.model.plus

@Composable
internal fun ProvisionerRoute(
    appState: AppState,
    provisioner: Provisioner,
    // provisionerData: ProvisionerData = ProvisionerData(provisioner),
    otherProvisioners: List<Provisioner>,
    onTtlChanged: (Int) -> Unit,
    save: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val provisionerData by remember(provisioner.uuid) {
        derivedStateOf { ProvisionerData(provisioner) }
    }
    var isCurrentlyEditable by rememberSaveable(inputs = arrayOf(provisioner.uuid)) {
        mutableStateOf(true)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
            .verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        SectionTitle(title = stringResource(id = R.string.label_provisioner))
        Name(
            name = provisionerData.name,
            onNameChanged = {
                provisioner.name = it
                save()
            },
            isCurrentlyEditable = isCurrentlyEditable,
            onEditableStateChanged = { isCurrentlyEditable = !isCurrentlyEditable })

        UnicastAddress(
            context = context,
            snackbarHostState = appState.snackbarHostState,
            keyboardController = keyboardController,
            provisioner = provisioner,
            address = provisionerData.address,
            isCurrentlyEditable = isCurrentlyEditable,
            onEditableStateChanged = { isCurrentlyEditable = !isCurrentlyEditable },
            save = save,
        )
        Ttl(
            keyboardController = keyboardController,
            ttl = provisionerData.ttl,
            onTtlChanged = onTtlChanged,
            isCurrentlyEditable = isCurrentlyEditable
        ) { isCurrentlyEditable = !isCurrentlyEditable }

        DeviceKey(key = provisionerData.deviceKey)
        SectionTitle(title = stringResource(R.string.label_allocated_ranges))
        UnicastRanges(
            snackbarHostState = appState.snackbarHostState,
            provisionerData = provisionerData,
            otherRanges = otherProvisioners.flatMap { it.allocatedUnicastRanges },
            allocate = {
                runCatching { provisioner.allocate(it) }
                    .onSuccess { save() }
            },
            updateRange = { range, newRange ->
                runCatching { provisioner.update(range = range, newRange = newRange) }
                    .onSuccess { save() }
            },
            onRemoved = {
                // TODO show snackbar
                runCatching { provisioner.remove(range = it) }
                    .onSuccess { save() }
            }
        )
        GroupRanges(
            snackbarHostState = appState.snackbarHostState,
            provisionerData = provisionerData,
            otherRanges = otherProvisioners.flatMap { it.allocatedGroupRanges },
            allocate = {
                runCatching { provisioner.allocate(range = it) }
                    .onSuccess { save() }
            },
            updateRange = { range, newRange ->
                runCatching { provisioner.update(range = range, newRange = newRange) }
                    .onSuccess { save() }
            },
            onRemoved = {
                // TODO show snackbar
                runCatching { provisioner.remove(range = it) }
                    .onSuccess { save() }
            }
        )
        SceneRanges(
            snackbarHostState = appState.snackbarHostState,
            provisionerData = provisionerData,
            otherRanges = otherProvisioners.flatMap { it.allocatedSceneRanges },
            allocate = {
                runCatching { provisioner.allocate(it) }
                    .onSuccess { save() }
            },
            updateRange = { range, newRange ->
                runCatching { provisioner.update(range = range, newRange = newRange) }
                    .onSuccess { save() }
            },
            onRemoved = {
                // TODO show snackbar
                runCatching { provisioner.remove(range = it) }
                    .onSuccess { save() }
            }
        )
        AddressRangeLegendsForProvisioner()
        Spacer(modifier = Modifier.size(16.dp))
    }
}


@Composable
fun Name(
    name: String,
    onNameChanged: (String) -> Unit,
    isCurrentlyEditable: Boolean,
    onEditableStateChanged: () -> Unit
) {
    ElevatedCardItemTextField(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Badge,
        title = stringResource(id = R.string.label_name),
        subtitle = name,
        placeholder = stringResource(id = R.string.label_placeholder_provisioner_name),
        onValueChanged = onNameChanged,
        isEditable = isCurrentlyEditable,
        onEditableStateChanged = onEditableStateChanged
    )
}

@Composable
private fun UnicastAddress(
    context: Context,
    snackbarHostState: SnackbarHostState,
    keyboardController: SoftwareKeyboardController?,
    provisioner: Provisioner,
    address: UnicastAddress?,
    isCurrentlyEditable: Boolean,
    onEditableStateChanged: () -> Unit,
    save: () -> Unit
) {
    val initialValue by remember { mutableStateOf(address?.toHexString() ?: "") }
    var value by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(text = initialValue, selection = TextRange(initialValue.length))
        )
    }
    var error by rememberSaveable { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var onEditClick by rememberSaveable { mutableStateOf(false) }
    var onUnassignClick by remember { mutableStateOf(false) }

    OutlinedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                modifier = Modifier.padding(start = 12.dp),
                imageVector = Icons.Outlined.Lan,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
            Crossfade(targetState = onEditClick, label = "Address") { state ->
                when (state) {
                    true -> MeshOutlinedTextField(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onFocus = onEditClick,
                        value = value,
                        onValueChanged = {
                            error = false
                            value = it
                            if (it.text.isNotEmpty()) {
                                if (UnicastAddress.isValid(it.text.toUShort(16))) {
                                    error = false
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                } else {
                                    error = true
                                    errorMessage = "Invalid unicast address"
                                }
                            }
                        },
                        label = { Text(text = stringResource(id = R.string.label_name)) },
                        internalTrailingIcon = {
                            IconButton(
                                enabled = value.text.isNotBlank(),
                                onClick = {
                                    value = TextFieldValue("", TextRange(0))
                                    error = false
                                },
                                content = {
                                    Icon(
                                        imageVector = Icons.Outlined.Clear,
                                        contentDescription = null
                                    )
                                }
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters
                        ),
                        regex = Regex("[0-9A-Fa-f]{0,4}"),
                        isError = error,
                        content = {
                            IconButton(
                                modifier = Modifier.padding(start = 8.dp),
                                enabled = address != null,
                                onClick = { onUnassignClick = !onUnassignClick },
                                content = {
                                    Icon(
                                        imageVector = Icons.Outlined.GppMaybe,
                                        contentDescription = null,
                                        tint = Color.Red.copy(alpha = 0.6f)
                                    )
                                }
                            )
                            IconButton(
                                modifier = Modifier.padding(start = 8.dp),
                                enabled = !error,
                                onClick = {
                                    if (value.text.isEmpty()) {
                                        value = TextFieldValue(
                                            text = initialValue,
                                            selection = TextRange(index = initialValue.length)
                                        )
                                        onEditClick = false
                                        error = false
                                        onEditableStateChanged()
                                    } else {
                                        runCatching {
                                            provisioner.assign(
                                                address = UnicastAddress(
                                                    address = value.text.toInt(
                                                        radix = 16
                                                    )
                                                )
                                            )
                                        }.onSuccess {
                                            error = false
                                            onEditClick = false
                                            onEditableStateChanged()
                                            save()
                                        }.onFailure { t ->
                                            error = true
                                            errorMessage = t.convertToString(context = context)
                                        }
                                    }
                                },
                                content = {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    )

                    false -> MeshTwoLineListItem(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        title = stringResource(id = R.string.label_unicast_address),
                        subtitle = address?.let {
                            "0x${value.text.uppercase()}"
                        } ?: stringResource(id = R.string.label_not_assigned),
                        trailingComposable = {
                            IconButton(
                                enabled = isCurrentlyEditable,
                                onClick = {
                                    error = false
                                    onEditClick = !onEditClick
                                    onEditableStateChanged()
                                },
                                content = {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = null,
                                        tint = LocalContentColor.current.copy(alpha = 0.6f)
                                    )
                                }
                            )
                        }
                    )
                }
            }
        }
    }

    if (onUnassignClick) {
        MeshAlertDialog(
            onDismissRequest = { onUnassignClick = !onUnassignClick },
            onConfirmClick = {
                error = !error
                keyboardController?.hide()
                onUnassignClick = !onUnassignClick
                onEditClick = !onEditClick
                onEditableStateChanged()
                provisioner.disableConfigurationCapabilities()
                save()
            },
            onDismissClick = { onUnassignClick = !onUnassignClick },
            icon = Icons.Outlined.GppMaybe,
            title = stringResource(R.string.label_unassign_address),
            text = stringResource(R.string.unassign_address_rationale)
        )
    }

    if (error && errorMessage.isNotEmpty()) {
        LaunchedEffect(key1 = snackbarHostState) {
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short
            )
        }
    }
}

@Composable
private fun Ttl(
    keyboardController: SoftwareKeyboardController?,
    ttl: Int?,
    onTtlChanged: (Int) -> Unit,
    isCurrentlyEditable: Boolean,
    onEditableStateChanged: () -> Unit,
) {
    ElevatedCardItemTextField(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Timer,
        title = stringResource(id = R.string.label_ttl),
        subtitle = ttl?.toString() ?: "0",
        onValueChanged = {
            keyboardController?.hide()
            if (it.isNotEmpty()) {
                onTtlChanged(it.toInt())
            }
        },
        isEditable = isCurrentlyEditable,
        onEditableStateChanged = onEditableStateChanged,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        )
    )
}

@Composable
private fun DeviceKey(key: String?) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.VpnKey,
        title = stringResource(id = R.string.label_device_key),
        subtitle = key ?: stringResource(R.string.label_not_applicable)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnicastRanges(
    snackbarHostState: SnackbarHostState,
    provisionerData: ProvisionerData,
    otherRanges: List<UnicastRange>,
    allocate: (Range) -> Unit,
    updateRange: (Range, Range) -> Unit,
    onRemoved: (Range) -> Unit
) {
    val scope = rememberCoroutineScope()
    var showUnicastRanges by rememberSaveable { mutableStateOf(false) }
    var ranges by remember(key1 = provisionerData.uuid) {
        mutableStateOf<List<Range>>(provisionerData.unicastRanges)
    }
    OutlinedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
        AllocatedRanges(
            imageVector = Icons.Outlined.Lan,
            title = stringResource(id = R.string.label_unicast_range),
            ranges = ranges,
            otherRanges = otherRanges,
            onClick = { showUnicastRanges = true }
        )
    }
    if (showUnicastRanges) {
        ModalBottomSheet(
            onDismissRequest = { showUnicastRanges = false },
            content = {
                RangesScreen(
                    snackbarHostState = snackbarHostState,
                    title = stringResource(id = R.string.label_unicast_ranges),
                    ranges = ranges,
                    otherRanges = otherRanges,
                    addRange = { start, end ->
                        scope.launch {
                            val range = (UnicastAddress(start)..UnicastAddress(end))
                            ranges = ranges + range
                            if (!ranges.overlaps(otherRanges)) {
                                allocate(range)
                            }
                        }
                    },
                    onRangeUpdated = { range, start, end ->
                        updateRange(range, (UnicastAddress(start)..UnicastAddress(end)))
                    },
                    onSwiped = { range ->
                        ranges = ranges - range as UnicastRange
                        onRemoved(range)
                    },
                    onUndoClicked = {},
                    remove = onRemoved,
                    isValidBound = { UnicastAddress.isValid(address = it) },
                    resolve = { ranges = ranges - otherRanges }
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupRanges(
    snackbarHostState: SnackbarHostState,
    provisionerData: ProvisionerData,
    otherRanges: List<GroupRange>,
    allocate: (Range) -> Unit,
    updateRange: (Range, Range) -> Unit,
    onRemoved: (Range) -> Unit
) {
    var showGroupRanges by rememberSaveable { mutableStateOf(false) }
    var ranges by remember(key1 = provisionerData.uuid) {
        mutableStateOf<List<Range>>(provisionerData.groupRanges)
    }
    OutlinedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
        AllocatedRanges(
            imageVector = Icons.Outlined.GroupWork,
            title = stringResource(id = R.string.label_group_range),
            ranges = ranges,
            otherRanges = otherRanges,
            onClick = { showGroupRanges = true }
        )
    }
    if (showGroupRanges) {
        ModalBottomSheet(
            onDismissRequest = { showGroupRanges = false },
            content = {
                RangesScreen(
                    snackbarHostState = snackbarHostState,
                    title = stringResource(id = R.string.label_group_ranges),
                    ranges = ranges,
                    otherRanges = otherRanges,
                    addRange = { start, end ->
                        val range = (GroupAddress(start)..GroupAddress(end))
                        ranges = ranges + range
                        if (!ranges.overlaps(otherRanges)) {
                            allocate(range)
                        }
                    },
                    onRangeUpdated = { range, start, end ->
                        updateRange(range, (GroupAddress(start)..GroupAddress(end)))
                    },
                    onSwiped = { range ->
                        ranges = ranges - range as GroupRange
                        onRemoved(range)
                    },
                    onUndoClicked = {},
                    remove = onRemoved,
                    isValidBound = { GroupAddress.isValid(address = it) },
                    resolve = { ranges = ranges - otherRanges }
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SceneRanges(
    snackbarHostState: SnackbarHostState,
    provisionerData: ProvisionerData,
    otherRanges: List<SceneRange>,
    allocate: (Range) -> Unit,
    updateRange: (Range, Range) -> Unit,
    onRemoved: (Range) -> Unit
) {
    var showSceneRanges by rememberSaveable { mutableStateOf(false) }
    var ranges by remember(key1 = provisionerData.uuid) {
        mutableStateOf<List<Range>>(
            provisionerData.sceneRanges
        )
    }
    OutlinedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
        AllocatedRanges(
            imageVector = Icons.Outlined.AutoAwesome,
            title = stringResource(id = R.string.label_scene_range),
            ranges = ranges,
            otherRanges = otherRanges,
            onClick = { showSceneRanges = true }
        )
    }
    if (showSceneRanges) {
        ModalBottomSheet(
            onDismissRequest = { showSceneRanges = false },
            content = {
                RangesScreen(
                    snackbarHostState = snackbarHostState,
                    title = stringResource(id = R.string.label_scene_ranges),
                    ranges = ranges,
                    otherRanges = otherRanges,
                    addRange = { start, end ->
                        val range = SceneRange(firstScene = start, lastScene = end)
                        ranges = ranges + range
                        if (!ranges.overlaps(otherRanges)) {
                            allocate(range)
                        }
                    },
                    onRangeUpdated = { range, start, end ->
                        updateRange(range, SceneRange(firstScene = start, lastScene = end))
                    },
                    onSwiped = { range ->
                        ranges = ranges - range as SceneRange
                        onRemoved(range)
                    },
                    onUndoClicked = {},
                    remove = onRemoved,
                    isValidBound = { Scene.isValid(sceneNumber = it) },
                    resolve = { ranges = ranges - otherRanges }
                )
            }
        )
    }
}