@file:OptIn(
    ExperimentalLifecycleComposeApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
)

package no.nordicsemi.android.nrfmesh.feature.provisioners

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
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
        disableConfigurationCapabilities = viewModel::disableConfigurationCapabilities,
        onTtlChanged = viewModel::onTtlChanged,
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
    onAddressChanged: (Int) -> Result<Unit>,
    disableConfigurationCapabilities: () -> Result<Unit>,
    onTtlChanged: (Int) -> Unit,
    onBackPressed: () -> Unit,
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
                    snackbarHostState = snackbarHostState,
                    padding = padding,
                    provisioner = provisionerState.provisioner,
                    otherProvisioners = provisionerState.otherProvisioners,
                    onNameChanged = onNameChanged,
                    onAddressChanged = onAddressChanged,
                    disableConfigurationCapabilities = disableConfigurationCapabilities,
                    onTtlChanged = onTtlChanged
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
    snackbarHostState: SnackbarHostState,
    padding: PaddingValues,
    provisioner: Provisioner,
    otherProvisioners: List<Provisioner>,
    onNameChanged: (String) -> Unit,
    onAddressChanged: (Int) -> Result<Unit>,
    disableConfigurationCapabilities: () -> Result<Unit>,
    onTtlChanged: (Int) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    var isCurrentlyEditable by rememberSaveable { mutableStateOf(true) }
    LazyColumn(
        contentPadding = padding,
        modifier = Modifier
            .padding(end = 16.dp)
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
                    context = context,
                    scope = scope,
                    snackbarHostState = snackbarHostState,
                    keyboardController = keyboardController,
                    address = node?.primaryUnicastAddress?.address,
                    onAddressChanged = onAddressChanged,
                    disableConfigurationCapabilities = disableConfigurationCapabilities,
                    isCurrentlyEditable = isCurrentlyEditable,
                    onEditableStateChanged = { isCurrentlyEditable = !isCurrentlyEditable }
                )
            }
            item {
                Ttl(
                    context = context,
                    scope = scope,
                    snackbarHostState = snackbarHostState,
                    keyboardController = keyboardController,
                    ttl = node?.defaultTTL,
                    onTtlChanged = onTtlChanged,
                    isCurrentlyEditable = isCurrentlyEditable,
                    onEditableStateChanged = { isCurrentlyEditable = !isCurrentlyEditable }
                )
            }
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
            item {
                Divider(modifier = Modifier.padding(vertical = 20.dp))
                AddressRangeLegendsForProvisioner()
                Spacer(modifier = Modifier.size(16.dp))
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
    var value by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(text = name, selection = TextRange(name.length))
        )
    }
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
                    IconButton(
                        enabled = value.text.isNotBlank(),
                        onClick = {
                            value = TextFieldValue(text = name, selection = TextRange(0))
                        }
                    ) { Icon(imageVector = Icons.Outlined.Clear, contentDescription = null) }
                },
                content = {
                    IconButton(
                        modifier = Modifier.padding(start = 8.dp, end = 16.dp),
                        enabled = value.text.isNotBlank(),
                        onClick = {
                            onEditClick = !onEditClick
                            onEditableStateChanged()
                            onNameChanged(value.text.trim())
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
                subtitle = value.text,
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
    context: Context,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    keyboardController: SoftwareKeyboardController?,
    address: Address?,
    onAddressChanged: (Int) -> Result<Unit>,
    disableConfigurationCapabilities: () -> Result<Unit>,
    isCurrentlyEditable: Boolean,
    onEditableStateChanged: () -> Unit,
) {
    val tempAddress by remember { mutableStateOf(address?.toHex() ?: "") }
    var value by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(text = tempAddress, selection = TextRange(tempAddress.length))
        )
    }
    var error by rememberSaveable { mutableStateOf(false) }
    var onEditClick by rememberSaveable { mutableStateOf(false) }
    var onUnassignClick by remember { mutableStateOf(false) }
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
                onValueChanged = {
                    error = false
                    value = it
                },
                label = { Text(text = stringResource(id = R.string.label_unicast_address)) },
                internalTrailingIcon = {
                    IconButton(
                        enabled = value.text.isNotBlank(),
                        onClick = {
                            value = TextFieldValue("", TextRange(0))
                            error = false
                        }
                    ) { Icon(imageVector = Icons.Outlined.Clear, contentDescription = null) }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters
                ),
                regex = Regex("[0-9A-Fa-f]{0,4}"),
                isError = error,
                content = {
                    IconButton(
                        modifier = Modifier.padding(start = 16.dp),
                        enabled = address != null,
                        onClick = { onUnassignClick = !onUnassignClick }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.GppMaybe,
                            contentDescription = null,
                            tint = Color.Red.copy(alpha = 0.6f)
                        )
                    }
                    IconButton(
                        modifier = Modifier.padding(end = 16.dp),
                        enabled = value.text.isNotEmpty(),
                        onClick = {
                            keyboardController?.hide()
                            if (value.text.isNotEmpty()) {
                                onAddressChanged(value.text.toInt(radix = 16))
                                    .onSuccess {
                                        onEditClick = !onEditClick
                                        onEditableStateChanged()
                                    }
                                    .onFailure {
                                        error = true
                                        showSnackbar(
                                            scope = scope,
                                            snackbarHostState = snackbarHostState,
                                            message = it.convertToString(context = context)
                                        )
                                    }
                            } else {
                                onEditClick = !onEditClick
                                onEditableStateChanged()
                            }
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
                subtitle = address?.toHex(prefix0x = true)
                    ?: stringResource(id = R.string.label_not_assigned),
                trailingComposable = {
                    IconButton(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        enabled = isCurrentlyEditable,
                        onClick = {
                            error = false
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

    if (onUnassignClick) {
        MeshAlertDialog(
            onDismissRequest = { onUnassignClick = !onUnassignClick },
            onConfirmClick = {
                error = !error
                keyboardController?.hide()
                onUnassignClick = !onUnassignClick
                onEditClick = !onEditClick
                onEditableStateChanged()
                disableConfigurationCapabilities()
            },
            onDismissClick = { onUnassignClick = !onUnassignClick },
            icon = Icons.Outlined.GppMaybe,
            title = stringResource(R.string.label_unassign_address),
            text = stringResource(R.string.unassign_address_rationale)
        )
    }
}

@Composable
private fun Ttl(
    context: Context,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    keyboardController: SoftwareKeyboardController?,
    ttl: Int?,
    onTtlChanged: (Int) -> Unit,
    isCurrentlyEditable: Boolean,
    onEditableStateChanged: () -> Unit,
) {
    var error by rememberSaveable { mutableStateOf(false) }
    var value by rememberSaveable { mutableStateOf(ttl?.toString() ?: "") }
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
                onValueChanged = {
                    error = false
                    value = it
                },
                label = { Text(text = stringResource(id = R.string.label_ttl)) },
                internalTrailingIcon = {
                    IconButton(
                        enabled = value.isNotBlank(),
                        onClick = {
                            value = ""
                            error = false
                        }
                    ) { Icon(imageVector = Icons.Outlined.Clear, contentDescription = null) }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType = KeyboardType.Number
                ),
                regex = Regex("[1-9]{0,3}"),
                isError = error,
                content = {
                    IconButton(
                        modifier = Modifier.padding(start = 8.dp, end = 16.dp),
                        onClick = {
                            keyboardController?.hide()
                            value = value.trim()
                            if (value.isNotEmpty()) {
                                onTtlChanged(value.toInt())
                            } else {
                                onEditClick = !onEditClick
                                onEditableStateChanged()
                            }
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
                title = stringResource(id = R.string.label_ttl),
                subtitle = ttl?.toString()
                    ?: stringResource(id = R.string.label_not_assigned),
                trailingComposable = {
                    IconButton(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        enabled = isCurrentlyEditable,
                        onClick = {
                            error = !error
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
    Ranges(
        imageVector = Icons.Outlined.Lan,
        title = stringResource(id = R.string.label_unicast_range),
        ranges = ranges,
        otherRanges = otherRanges
    )
}

@Composable
private fun GroupRange(ranges: List<GroupRange>, otherRanges: List<GroupRange>) {
    Ranges(
        imageVector = Icons.Outlined.GroupWork,
        title = stringResource(id = R.string.label_group_range),
        ranges = ranges,
        otherRanges = otherRanges
    )
}


@Composable
private fun SceneRange(ranges: List<SceneRange>, otherRanges: List<SceneRange>) {
    Ranges(
        imageVector = Icons.Outlined.AutoAwesome,
        title = stringResource(id = R.string.label_scene_range),
        ranges = ranges,
        otherRanges = otherRanges
    )
}

@Composable
private fun Ranges(
    imageVector: ImageVector,
    title: String,
    ranges: List<Range>,
    otherRanges: List<Range>,
) {
    TwoLineRangeListItem(
        leadingComposable = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = imageVector,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        title = title,
        lineTwo = {
            val ownRangeColor = MaterialTheme.colorScheme.primary
            val otherRangeColor = Color.DarkGray
            val conflictingColor = Color.Red
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .height(height = 16.dp)
                    .background(color = Color.LightGray)
            ) {
                // Mark own ranges
                markRanges(
                    color = ownRangeColor,
                    ranges = ranges
                )
                // Mark other provisioners' ranges
                markRanges(
                    color = otherRangeColor,
                    ranges = otherRanges
                )
                // Mark conflicting ranges
                markRanges(
                    color = conflictingColor,
                    ranges = ranges.intersect(otherRanges.toSet()).toList()
                )
            }
        }
    )
}

private fun DrawScope.markRanges(
    color: Color,
    ranges: List<Range>
) {
    ranges.forEach { range ->
        when (range) {
            is UnicastRange -> {
                markRange(
                    color = color,
                    lowAddress = range.lowAddress.address.toInt(),
                    highAddress = range.highAddress.address.toInt(),
                    lowerBound = minUnicastAddress.toInt(),
                    upperBound = maxUnicastAddress.toInt()
                )
            }
            is GroupRange -> {
                markRange(
                    color = color,
                    lowAddress = range.lowAddress.address.toInt(),
                    highAddress = range.highAddress.address.toInt(),
                    lowerBound = 0xC000u.toInt(),
                    upperBound = 0xFEFFu.toInt()
                )
            }
            is SceneRange -> {
                markRange(
                    color = color,
                    lowAddress = range.firstScene.toInt(),
                    highAddress = range.lastScene.toInt(),
                    lowerBound = 0x0001u.toInt(),
                    upperBound = 0xFFFFu.toInt()
                )
            }
        }
    }
}

private fun DrawScope.markRange(
    color: Color,
    lowAddress: Int,
    highAddress: Int,
    lowerBound: Int,
    upperBound: Int
) {
    size.let { size ->
        val rangeWidth = size.width * (highAddress - lowAddress) / (upperBound - lowerBound)
        val rangeStart = size.width * (lowAddress - lowerBound) / (upperBound - lowerBound)
        drawRect(
            color = color,
            topLeft = Offset(x = rangeStart, y = 0f),
            size = Size(width = rangeWidth.inc(), height = size.height),
            style = Fill
        )
    }
}