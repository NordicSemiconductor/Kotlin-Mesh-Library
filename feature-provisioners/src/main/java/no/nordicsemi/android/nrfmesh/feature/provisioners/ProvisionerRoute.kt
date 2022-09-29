@file:OptIn(ExperimentalLifecycleComposeApi::class, ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.feature.provisioners

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.feature.provisioners.R
import no.nordicsemi.android.nrfmesh.core.ui.*
import no.nordicsemi.kotlin.mesh.core.model.*
import no.nordicsemi.kotlin.mesh.crypto.Utils.encodeHex

@Composable
internal fun ProvisionerRoute(
    viewModel: ProvisionerViewModel = hiltViewModel(),
    onBackPressed: () -> Unit
) {
    val uiState: ProvisionerScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProvisionerScreen(
        provisionerState = uiState.provisionerState,
        onNameChanged = viewModel::onNameChanged,
        onAddressChanged = viewModel::onAddressChanged,
        onBackPressed = {
            viewModel.save()
            onBackPressed()
        }
    )
}

@Composable
private fun ProvisionerScreen(
    provisionerState: ProvisionerState,
    onNameChanged: (String) -> Unit,
    onAddressChanged: (String) -> Unit,
    onBackPressed: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(connection = scrollBehavior.nestedScrollConnection),
        topBar = {
            MeshLargeTopAppBar(
                title = stringResource(id = R.string.label_edit_provisioner),
                navigationIcon = {
                    IconButton(onClick = { onBackPressed() }) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (provisionerState) {
            ProvisionerState.Loading -> { /* Do nothing */
            }
            is ProvisionerState.Success -> {
                ProvisionerInfo(
                    padding = padding,
                    provisioner = provisionerState.provisioner,
                    otherProvisioners = provisionerState.otherProvisioners,
                    onNameChanged = onNameChanged,
                    onAddressChanged = onAddressChanged
                )
            }
            is ProvisionerState.Error -> {
                MeshNoItemsAvailable(
                    imageVector = Icons.Outlined.Group,
                    title = provisionerState.throwable.message ?: "Unknown error"
                )
            }
        }
    }
}

@Composable
private fun ProvisionerInfo(
    padding: PaddingValues,
    provisioner: Provisioner,
    otherProvisioners: List<Provisioner>,
    onNameChanged: (String) -> Unit,
    onAddressChanged: (String) -> Unit
) {
    var isCurrentlyEditable by rememberSaveable { mutableStateOf(true) }
    LazyColumn(
        contentPadding = padding,
        modifier = Modifier
            .fillMaxSize()
    ) {
        provisioner.run {
            item {
                Name(
                    name = name,
                    onNameChanged = onNameChanged,
                    isCurrentlyEditable = isCurrentlyEditable,
                    onEditableStateChanged = { isCurrentlyEditable = !isCurrentlyEditable })
            }
            item {
                UnicastAddress(
                    address = node?.primaryUnicastAddress?.address,
                    onAddressChanged = onAddressChanged,
                    isCurrentlyEditable = isCurrentlyEditable,
                    onEditableStateChanged = { isCurrentlyEditable = !isCurrentlyEditable }
                )
            }
            item { Ttl(ttl = node?.defaultTTL) }
            item { DeviceKey(key = provisioner.node?.deviceKey) }
            item { SectionTitle(title = stringResource(R.string.label_allocated_ranges)) }
            item {
                UnicastRange(
                    ranges = provisioner.allocatedUnicastRanges,
                    otherRanges = otherProvisioners.flatMap { it.allocatedUnicastRanges }
                )
            }
            item {
                GroupRange(
                    ranges = provisioner.allocatedGroupRanges,
                    otherRanges = otherProvisioners.flatMap { it.allocatedGroupRanges }
                )
            }
            item {
                SceneRange(
                    ranges = provisioner.allocatedSceneRanges,
                    otherRanges = otherProvisioners.flatMap { it.allocatedSceneRanges }
                )
            }
        }
    }
}


@Composable
fun Name(
    name: String,
    onNameChanged: (String) -> Unit,
    isCurrentlyEditable: Boolean,
    onEditableStateChanged: () -> Unit,
) {
    var value by rememberSaveable { mutableStateOf(name) }
    var onEditClick by rememberSaveable { mutableStateOf(false) }
    Crossfade(targetState = onEditClick) { state ->
        when (state) {
            true -> MeshOutlinedTextField(
                modifier = Modifier.padding(vertical = 8.dp),
                onFocus = onEditClick,
                externalLeadingIcon = {
                    Icon(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        imageVector = Icons.Outlined.Badge,
                        contentDescription = null,
                        tint = LocalContentColor.current.copy(alpha = 0.6f)
                    )
                },
                value = value,
                onValueChanged = { value = it },
                label = { Text(text = stringResource(id = R.string.label_name)) },
                placeholder = { Text(text = stringResource(id = R.string.label_placeholder_provisioner_name)) },
                internalTrailingIcon = {
                    IconButton(enabled = value.isNotBlank(), onClick = { value = "" }) {
                        Icon(imageVector = Icons.Outlined.Clear, contentDescription = null)
                    }
                },
                content = {
                    IconButton(
                        modifier = Modifier.padding(start = 8.dp, end = 16.dp),
                        enabled = value.isNotBlank(),
                        onClick = {
                            onEditClick = !onEditClick
                            onEditableStateChanged()
                            value = value.trim()
                            onNameChanged(value)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            tint = LocalContentColor.current.copy(alpha = 0.6f)
                        )
                    }
                }
            )
            false -> MeshTwoLineListItem(
                leadingComposable = {
                    Icon(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        imageVector = Icons.Outlined.Badge,
                        contentDescription = null,
                        tint = LocalContentColor.current.copy(alpha = 0.6f)
                    )
                },
                title = stringResource(id = R.string.label_name),
                subtitle = value,
                trailingComposable = {
                    IconButton(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        enabled = isCurrentlyEditable,
                        onClick = {
                            onEditClick = !onEditClick
                            onEditableStateChanged()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            tint = LocalContentColor.current.copy(alpha = 0.6f)
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun UnicastAddress(
    address: Address?,
    onAddressChanged: (String) -> Unit,
    isCurrentlyEditable: Boolean,
    onEditableStateChanged: () -> Unit,
) {
    var value by rememberSaveable { mutableStateOf(address?.toString() ?: "") }
    var onEditClick by rememberSaveable { mutableStateOf(false) }
    Crossfade(targetState = onEditClick) { state ->
        when (state) {
            true -> MeshOutlinedTextField(
                modifier = Modifier.padding(vertical = 8.dp),
                onFocus = onEditClick,
                externalLeadingIcon = {
                    Icon(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        imageVector = Icons.Outlined.Badge,
                        contentDescription = null,
                        tint = LocalContentColor.current.copy(alpha = 0.6f)
                    )
                },
                value = value,
                onValueChanged = { value = it },
                label = { Text(text = stringResource(id = R.string.label_unicast_address)) },
                internalTrailingIcon = {
                    IconButton(enabled = value.isNotBlank(), onClick = { value = "" }) {
                        Icon(imageVector = Icons.Outlined.Clear, contentDescription = null)
                    }
                },
                content = {
                    IconButton(
                        modifier = Modifier.padding(start = 8.dp, end = 16.dp),
                        enabled = value.isNotBlank(),
                        onClick = {
                            onEditClick = !onEditClick
                            onEditableStateChanged()
                            value = value.trim()
                            onAddressChanged(value)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            tint = LocalContentColor.current.copy(alpha = 0.6f)
                        )
                    }
                }
            )
            false -> MeshTwoLineListItem(
                leadingComposable = {
                    Icon(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        imageVector = Icons.Outlined.Lan,
                        contentDescription = null,
                        tint = LocalContentColor.current.copy(alpha = 0.6f)
                    )
                },
                title = stringResource(id = R.string.label_unicast_address),
                subtitle = address?.toString() ?: stringResource(id = R.string.label_not_assigned),
                trailingComposable = {
                    IconButton(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        enabled = isCurrentlyEditable,
                        onClick = {
                            onEditClick = !onEditClick
                            onEditableStateChanged()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            tint = LocalContentColor.current.copy(alpha = 0.6f)
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun Ttl(ttl: Int?) {
    MeshTwoLineListItem(
        leadingComposable = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.HourglassTop,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        title = stringResource(id = R.string.label_ttl),
        subtitle = ttl?.toString() ?: stringResource(R.string.label_not_applicable)
    )
}

@Composable
private fun DeviceKey(key: ByteArray?) {
    MeshTwoLineListItem(
        leadingComposable = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.VpnKey,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        title = stringResource(id = R.string.label_device_key),
        subtitle = key?.encodeHex() ?: stringResource(R.string.label_not_applicable)
    )
}


@Composable
private fun UnicastRange(ranges: List<UnicastRange>, otherRanges: List<UnicastRange>) {
    MeshTwoLineListItem(
        leadingComposable = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.Lan,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        title = stringResource(id = R.string.label_unicast_range),
        trailingComposable = { Range(ranges = ranges, otherRanges = otherRanges) }
    )
}

@Composable
private fun GroupRange(ranges: List<GroupRange>, otherRanges: List<GroupRange>) {
    MeshTwoLineListItem(
        leadingComposable = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.GroupWork,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        title = stringResource(id = R.string.label_group_range),
        trailingComposable = { Range(ranges = ranges, otherRanges = otherRanges) }
    )
}


@Composable
private fun SceneRange(ranges: List<SceneRange>, otherRanges: List<SceneRange>) {
    MeshTwoLineListItem(
        leadingComposable = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        title = stringResource(id = R.string.label_scene_range),
        trailingComposable = { Range(ranges = ranges, otherRanges = otherRanges) }
    )
}

@Composable
private fun Range(ranges: List<Range>, otherRanges: List<Range> = listOf()) {
    var parentSize by remember { mutableStateOf(Size.Zero) }
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .height(height = 20.dp)
            .fillMaxWidth(0.5f)
            .border(width = 0.5.dp, color = MaterialTheme.colorScheme.tertiary)
            .background(color = Color.LightGray)
            .onGloballyPositioned { parentSize = it.size.toSize() }

    ) {
        // Mark own range
        MarkRange(
            parentSize = parentSize,
            color = MaterialTheme.colorScheme.primary,
            ranges = ranges
        )
        // Mark other ranges
        MarkRange(
            parentSize = parentSize,
            color = Color.DarkGray,
            ranges = otherRanges
        )
        // Mark conflicting ranges
        MarkConflictingRange(parentSize = parentSize, ranges = ranges, otherRanges = otherRanges)
    }
}

@Composable
private fun MarkRange(
    parentSize: Size,
    color: Color,
    ranges: List<Range>
) {
    ranges.forEach {
        when (it) {
            is UnicastRange -> {
                MarkRange(
                    parentSize = parentSize,
                    color = color,
                    low = it.lowAddress.address.toInt(),
                    high = it.highAddress.address.toInt(),
                    lowerBound = 0x0001u.toInt(),
                    upperBound = 0x7FFFu.toInt()
                )
            }
            is GroupRange -> {
                MarkRange(
                    parentSize = parentSize,
                    color = color,
                    low = it.lowAddress.address.toInt(),
                    high = it.highAddress.address.toInt(),
                    lowerBound = 0xC000u.toInt(),
                    upperBound = 0xFEFFu.toInt()
                )
            }
            is SceneRange -> {
                MarkRange(
                    parentSize = parentSize,
                    color = color,
                    low = it.firstScene.toInt(),
                    high = it.lastScene.toInt(),
                    lowerBound = 0x0001u.toInt(),
                    upperBound = 0xFFFFu.toInt()
                )
            }
        }
    }
}

@Composable
private fun MarkConflictingRange(
    parentSize: Size,
    ranges: List<Range>,
    otherRanges: List<Range>
) {
    ranges.forEach { range ->
        otherRanges.forEach { otherRange ->
            when {
                range is UnicastRange && otherRange is UnicastRange -> {
                    if (range.overlaps(otherRange))
                        MarkRange(
                            parentSize = parentSize,
                            color = Color.Red,
                            low = otherRange.lowAddress.address.toInt(),
                            high = otherRange.highAddress.address.toInt(),
                            lowerBound = 0x0001u.toInt(),
                            upperBound = 0x7FFFu.toInt()
                        )
                }
                range is GroupRange && otherRange is GroupRange -> {
                    if (range.overlaps(otherRange))
                        MarkRange(
                            parentSize = parentSize,
                            color = Color.Red,
                            low = otherRange.lowAddress.address.toInt(),
                            high = otherRange.highAddress.address.toInt(),
                            lowerBound = 0xC000u.toInt(),
                            upperBound = 0xFEFFu.toInt()
                        )
                }
                range is SceneRange && otherRange is SceneRange -> {
                    if (range.overlaps(otherRange))
                        MarkRange(
                            parentSize = parentSize,
                            color = Color.Red,
                            low = otherRange.firstScene.toInt(),
                            high = otherRange.lastScene.toInt(),
                            lowerBound = 0x0001u.toInt(),
                            upperBound = 0xFFFFu.toInt()
                        )
                }
            }
        }
    }
}

@Composable
private fun MarkRange(
    parentSize: Size,
    color: Color,
    low: Int,
    high: Int,
    lowerBound: Int,
    upperBound: Int
) {
    val unit = parentSize.width / (upperBound - lowerBound).toFloat()
    val start = (low - lowerBound) * unit
    val end = (high - lowerBound) * unit
    Box(
        modifier = Modifier
            .offset(x = start.dp)
            .width(width = (end - start).dp)
            .fillMaxHeight()
            .background(color = color)
    )
}