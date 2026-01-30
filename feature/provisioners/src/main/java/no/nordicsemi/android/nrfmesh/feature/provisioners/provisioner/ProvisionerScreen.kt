package no.nordicsemi.android.nrfmesh.feature.provisioners.provisioner

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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GppMaybe
import androidx.compose.material.icons.outlined.GroupWork
import androidx.compose.material.icons.outlined.Lan
import androidx.compose.material.icons.outlined.RemoveModerator
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.common.convertToString
import no.nordicsemi.android.nrfmesh.core.data.models.ProvisionerData
import no.nordicsemi.android.nrfmesh.core.ui.AddressRangeLegendsForProvisioner
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItemTextField
import no.nordicsemi.android.nrfmesh.core.ui.MeshAlertDialog
import no.nordicsemi.android.nrfmesh.core.ui.MeshOutlinedHexTextField
import no.nordicsemi.android.nrfmesh.core.ui.MeshTwoLineListItem
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.feature.provisioners.R
import no.nordicsemi.android.nrfmesh.feature.provisioners.provisioner.ranges.AllocatedRanges
import no.nordicsemi.android.nrfmesh.feature.provisioners.provisioner.ranges.RangesScreen
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
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@Composable
internal fun ProvisionerScreen(
    snackbarHostState: SnackbarHostState,
    uiState: ProvisionerScreenUiState,
    moveProvisioner: (Provisioner, Int) -> Unit,
    save: () -> Unit,
) {
    when(uiState.provisionerState){
        is ProvisionerState.Success -> {
            ProvisionerContent(
                snackbarHostState = snackbarHostState,
                index = uiState.index,
                provisioner = uiState.provisionerState.provisioner,
                provisionerData = uiState.provisionerState.provisionerData,
                moveProvisioner = moveProvisioner,
                save = save
            )
        }
        else -> {}
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
private fun ProvisionerContent(
    snackbarHostState: SnackbarHostState,
    index: Int,
    provisioner: Provisioner,
    provisionerData: ProvisionerData,
    moveProvisioner: (Provisioner, Int) -> Unit,
    save: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    var isCurrentlyEditable by rememberSaveable(inputs = arrayOf(provisioner.uuid)) {
        mutableStateOf(true)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        SectionTitle(
            modifier = Modifier.padding(horizontal = 16.dp).padding(top = 8.dp),
            title = stringResource(id = R.string.label_provisioner)
        )
        Name(
            name = provisionerData.name,
            onNameChanged = {
                provisioner.name = it
                save()
            },
            isCurrentlyEditable = isCurrentlyEditable,
            onEditableStateChanged = { isCurrentlyEditable = !isCurrentlyEditable }
        )
        UnicastAddress(
            snackbarHostState = snackbarHostState,
            keyboardController = keyboardController,
            provisioner = provisioner,
            address = provisionerData.address,
            isCurrentlyEditable = isCurrentlyEditable,
            onEditableStateChanged = { isCurrentlyEditable = !isCurrentlyEditable },
            save = save,
        )
        if (provisionerData.hasConfigurationCapabilities) {
            DeviceKey(key = provisionerData.deviceKey)
        }
        if (index != 0) {
            MoveProvisioner(
                context = LocalContext.current,
                scope = scope,
                snackbarHostState = snackbarHostState,
                index = index,
                provisioner = provisioner,
                moveProvisioner = moveProvisioner
            )
        }
        SectionTitle(
            modifier = Modifier.padding(horizontal = 16.dp),
            title = stringResource(R.string.label_allocated_ranges)
        )
        UnicastRanges(
            scope = scope,
            snackbarHostState = snackbarHostState,
            provisioner = provisioner,
            provisionerData = provisionerData,
            otherRanges = provisioner.otherUnicastRanges,
            save = save
        )
        GroupRanges(
            scope = scope,
            snackbarHostState = snackbarHostState,
            provisioner = provisioner,
            provisionerData = provisionerData,
            otherRanges = provisioner.otherGroupRanges,
            save = save
        )
        SceneRanges(
            scope = scope,
            snackbarHostState = snackbarHostState,
            provisioner = provisioner,
            provisionerData = provisionerData,
            otherRanges = provisioner.otherSceneRanges,
            save = save
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
    onEditableStateChanged: () -> Unit,
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

@OptIn(ExperimentalUuidApi::class)
@Composable
private fun UnicastAddress(
    snackbarHostState: SnackbarHostState,
    keyboardController: SoftwareKeyboardController?,
    provisioner: Provisioner,
    address: UnicastAddress?,
    isCurrentlyEditable: Boolean,
    onEditableStateChanged: () -> Unit,
    save: () -> Unit,
) {
    val context = LocalContext.current
    val initialValue by remember(key1 = provisioner.uuid) {
        mutableStateOf(address?.toHexString() ?: "")
    }
    var value by rememberSaveable(
        stateSaver = TextFieldValue.Saver,
        inputs = arrayOf(provisioner.uuid)
    ) {
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
                tint = MaterialTheme.colorScheme.primary
            )
            Crossfade(targetState = onEditClick, label = "Address") { state ->
                when (state) {
                    true -> MeshOutlinedHexTextField(
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
                        label = {
                            Text(
                                text = stringResource(id = R.string.label_unicast_address),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        internalTrailingIcon = {
                            IconButton(
                                enabled = value.text.isNotBlank(),
                                onClick = {
                                    value = TextFieldValue("", TextRange(0))
                                    error = false
                                },
                                content = {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteSweep,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters
                        ),
                        regex = Regex("^[0-9A-Fa-f]{0,4}$"),
                        isError = error,
                        content = {
                            IconButton(
                                modifier = Modifier.padding(start = 8.dp),
                                onClick = {
                                    onEditClick = !onEditClick
                                    onEditableStateChanged()
                                    value = if (provisioner.hasConfigurationCapabilities) {
                                        TextFieldValue(
                                            text = address?.toHexString() ?: "",
                                            selection = TextRange(
                                                index = (address?.toHexString() ?: "").length
                                            )
                                        )
                                    } else {
                                        TextFieldValue(
                                            text = initialValue,
                                            selection = TextRange(index = initialValue.length)
                                        )
                                    }
                                },
                                content = {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            )
                            IconButton(
                                enabled = value.text.isNotEmpty(),
                                onClick = {
                                    keyboardController?.hide()
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
                                                    address = value.text.toUShort(radix = 16)
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
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            )
                            IconButton(
                                onClick = { onUnassignClick = !onUnassignClick },
                                content = {
                                    Icon(
                                        imageVector = Icons.Outlined.RemoveModerator,
                                        contentDescription = null,
                                        tint = Color.Red
                                    )
                                }
                            )
                        }
                    )

                    false -> MeshTwoLineListItem(
                        modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                        title = stringResource(id = R.string.label_unicast_address),
                        subtitle = value.text
                            .takeIf { it.isNotEmpty() }
                            ?.let { "0x${it.uppercase()}" }
                            ?: stringResource(id = R.string.label_not_assigned),
                        trailingComposable = {
                            IconButton(
                                modifier = Modifier.padding(start = 8.dp),
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
                                        tint = MaterialTheme.colorScheme.primary
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
                error = false
                if (isCurrentlyEditable && onEditClick)
                    onEditableStateChanged()
                onEditClick = false
                keyboardController?.hide()
                value = TextFieldValue(text = "", selection = TextRange(0))
                onUnassignClick = !onUnassignClick
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
private fun DeviceKey(key: String?) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.VpnKey,
        title = stringResource(id = R.string.label_device_key),
        subtitle = key ?: stringResource(R.string.label_not_applicable)
    )
}

@Composable
fun MoveProvisioner(
    context: Context,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    index: Int,
    provisioner: Provisioner,
    moveProvisioner: (Provisioner, Int) -> Unit,
) {
    var checked by remember { mutableStateOf(index == 0) }
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Badge,
        title = stringResource(R.string.label_set_as_local_provisioner),
        titleAction = {
            Checkbox(
                checked = checked,
                onCheckedChange = {
                    moveProvisioner(provisioner, if (it) 0 else index)
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(
                                R.string.message_local_provisioner_set,
                                provisioner.name
                            ),
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
private fun UnicastRanges(
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    provisioner: Provisioner,
    provisionerData: ProvisionerData,
    otherRanges: List<UnicastRange>,
    save: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var showUnicastRanges by rememberSaveable { mutableStateOf(false) }
    var ranges by remember(key1 = provisionerData.uuid) {
        mutableStateOf(provisionerData.unicastRanges.toMutableList<Range>())
    }
    val overlaps by remember {
        derivedStateOf { ranges.overlaps(other = otherRanges) }
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
            sheetState = sheetState,
            properties = ModalBottomSheetProperties(
                shouldDismissOnBackPress = !overlaps,
                shouldDismissOnClickOutside = !overlaps
            ),
            onDismissRequest = { showUnicastRanges = false },
            sheetGesturesEnabled = !overlaps,
            content = {
                RangesScreen(
                    snackbarHostState = snackbarHostState,
                    title = stringResource(id = R.string.label_unicast_ranges),
                    ranges = ranges,
                    otherRanges = otherRanges,
                    overlaps = overlaps,
                    addRange = { start, end ->
                        val range = UnicastAddress(address = start)..UnicastAddress(address = end)
                        ranges = ranges.plus(other = range).toMutableList()
                    },
                    onRangeUpdated = { start, end ->
                        val newRange =
                            UnicastAddress(address = start)..UnicastAddress(address = end)
                        ranges = ranges.plus(other = newRange).toMutableList()
                    },
                    onSwiped = { ranges = ranges.minus(other = it).toMutableList() },
                    isValidBound = { UnicastAddress.isValid(address = it) },
                    resolve = {
                        ranges = ranges.minus(other = otherRanges).toMutableList()

                    },
                    save = {
                        runCatching {
                            provisioner.allocate(ranges = ranges)
                        }.onSuccess {
                            save()
                            scope.launch {
                                sheetState.hide()
                            }.invokeOnCompletion {
                                if(!sheetState.isVisible){
                                    showUnicastRanges = false
                                }
                            }
                        }.onFailure {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = it.message ?: "Failed to allocate ranges",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
private fun GroupRanges(
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    provisioner: Provisioner,
    provisionerData: ProvisionerData,
    otherRanges: List<GroupRange>,
    save: () -> Unit,
) {
    var showGroupRanges by rememberSaveable { mutableStateOf(false) }
    var ranges by remember(key1 = provisionerData.uuid) {
        mutableStateOf(provisionerData.groupRanges.toMutableList<Range>())
    }
    val overlaps by remember {
        derivedStateOf { ranges.overlaps(other = otherRanges) }
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
                    overlaps = overlaps,
                    addRange = { start, end ->
                        val range = GroupAddress(address = start)..GroupAddress(address = end)
                        ranges = ranges.plus(other = range).toMutableList()
                    },
                    onRangeUpdated = { start, end ->
                        val newRange = GroupAddress(address = start)..GroupAddress(address = end)
                        ranges = ranges.plus(other = newRange).toMutableList()
                    },
                    onSwiped = { ranges = ranges.minus(other = it).toMutableList() },
                    isValidBound = { GroupAddress.isValid(address = it) },
                    resolve = { ranges = ranges.minus(other = otherRanges).toMutableList() },
                    save = {
                        runCatching {
                            provisioner.allocate(ranges = ranges)
                        }.onSuccess {
                            save()
                        }.onFailure {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = it.message ?: "Failed to allocate ranges",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
private fun SceneRanges(
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    provisioner: Provisioner,
    provisionerData: ProvisionerData,
    otherRanges: List<SceneRange>,
    save: () -> Unit,
) {
    var showSceneRanges by rememberSaveable { mutableStateOf(false) }
    var ranges by remember(key1 = provisionerData.uuid) {
        mutableStateOf(provisionerData.sceneRanges.toMutableList<Range>())
    }
    val overlaps by remember {
        derivedStateOf { ranges.overlaps(other = otherRanges) }
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
                    overlaps = overlaps,
                    addRange = { start, end ->
                        val range = SceneRange(firstScene = start, lastScene = end)
                        ranges = ranges.plus(other = range).toMutableList()
                    },
                    onRangeUpdated = { start, end ->
                        val newRange = SceneRange(firstScene = start, lastScene = end)
                        ranges = ranges.plus(other = newRange).toMutableList()
                    },
                    onSwiped = { ranges = ranges.minus(other = it).toMutableList() },
                    isValidBound = { Scene.isValid(sceneNumber = it) },
                    resolve = { ranges = ranges.minus(other = otherRanges).toMutableList() },
                    save = {
                        runCatching {
                            provisioner.allocate(ranges = ranges)
                        }.onSuccess {
                            save()
                        }.onFailure {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = it.message ?: "Failed to allocate ranges",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                )
            }
        )
    }
}