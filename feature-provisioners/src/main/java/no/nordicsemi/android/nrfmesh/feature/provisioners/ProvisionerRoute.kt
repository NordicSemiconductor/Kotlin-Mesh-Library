package no.nordicsemi.android.nrfmesh.feature.provisioners

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GppMaybe
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.GroupWork
import androidx.compose.material.icons.outlined.Lan
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.nrfmesh.core.common.convertToString
import no.nordicsemi.android.nrfmesh.core.ui.AddressRangeLegendsForProvisioner
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItemTextField
import no.nordicsemi.android.nrfmesh.core.ui.MeshAlertDialog
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.android.nrfmesh.core.ui.MeshOutlinedTextField
import no.nordicsemi.android.nrfmesh.core.ui.MeshTwoLineListItem
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.kotlin.data.toHexString
import no.nordicsemi.kotlin.mesh.core.model.Address
import no.nordicsemi.kotlin.mesh.core.model.GroupRange
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import no.nordicsemi.kotlin.mesh.core.model.SceneRange
import no.nordicsemi.kotlin.mesh.core.model.UnicastRange
import java.util.UUID

@Composable
internal fun ProvisionerRoute(
    viewModel: ProvisionerViewModel = hiltViewModel(),
    navigateToUnicastRanges: (UUID) -> Unit,
    navigateToGroupRanges: (UUID) -> Unit,
    navigateToSceneRanges: (UUID) -> Unit
) {
    val uiState: ProvisionerScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProvisionerScreen(
        provisionerState = uiState.provisionerState,
        onNameChanged = viewModel::onNameChanged,
        onAddressChanged = viewModel::onAddressChanged,
        disableConfigurationCapabilities = viewModel::disableConfigurationCapabilities,
        onTtlChanged = viewModel::onTtlChanged,
        isValidAddress = viewModel::isValidAddress,
        navigateToUnicastRanges = navigateToUnicastRanges,
        navigateToGroupRanges = navigateToGroupRanges,
        navigateToSceneRanges = navigateToSceneRanges
    )
}

@Composable
private fun ProvisionerScreen(
    provisionerState: ProvisionerState,
    onNameChanged: (String) -> Unit,
    onAddressChanged: (Int) -> Unit,
    isValidAddress: (UShort) -> Boolean,
    disableConfigurationCapabilities: () -> Unit,
    onTtlChanged: (Int) -> Unit,
    navigateToUnicastRanges: (UUID) -> Unit,
    navigateToGroupRanges: (UUID) -> Unit,
    navigateToSceneRanges: (UUID) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {

        when (provisionerState) {
            ProvisionerState.Loading -> { /* Do nothing */
            }

            is ProvisionerState.Success -> {
                ProvisionerInfo(
                    paddingValues = it,
                    snackbarHostState = snackbarHostState,
                    provisioner = provisionerState.provisioner,
                    otherProvisioners = provisionerState.otherProvisioners,
                    onNameChanged = onNameChanged,
                    onAddressChanged = onAddressChanged,
                    isValidAddress = isValidAddress,
                    disableConfigurationCapabilities = disableConfigurationCapabilities,
                    onTtlChanged = onTtlChanged,
                    navigateToUnicastRanges = navigateToUnicastRanges,
                    navigateToGroupRanges = navigateToGroupRanges,
                    navigateToSceneRanges = navigateToSceneRanges
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
    paddingValues: PaddingValues,
    snackbarHostState: SnackbarHostState,
    provisioner: Provisioner,
    otherProvisioners: List<Provisioner>,
    onNameChanged: (String) -> Unit,
    onAddressChanged: (Int) -> Unit,
    isValidAddress: (UShort) -> Boolean,
    disableConfigurationCapabilities: () -> Unit,
    onTtlChanged: (Int) -> Unit,
    navigateToUnicastRanges: (UUID) -> Unit,
    navigateToGroupRanges: (UUID) -> Unit,
    navigateToSceneRanges: (UUID) -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var isCurrentlyEditable by rememberSaveable { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
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
                    snackbarHostState = snackbarHostState,
                    keyboardController = keyboardController,
                    address = node?.primaryUnicastAddress?.address,
                    onAddressChanged = onAddressChanged,
                    isValidAddress = isValidAddress,
                    disableConfigurationCapabilities = disableConfigurationCapabilities,
                    isCurrentlyEditable = isCurrentlyEditable
                ) { isCurrentlyEditable = !isCurrentlyEditable }
            }
            item {
                Ttl(
                    keyboardController = keyboardController,
                    ttl = node?.defaultTTL?.toInt(),
                    onTtlChanged = onTtlChanged,
                    isCurrentlyEditable = isCurrentlyEditable
                ) { isCurrentlyEditable = !isCurrentlyEditable }
            }
            item { DeviceKey(key = provisioner.node?.deviceKey) }
            item { SectionTitle(title = stringResource(R.string.label_allocated_ranges)) }
            item {
                UnicastRange(
                    ranges = provisioner.allocatedUnicastRanges,
                    otherRanges = otherProvisioners.flatMap { it.allocatedUnicastRanges },
                    navigateToRanges = { navigateToUnicastRanges(provisioner.uuid) }
                )
            }
            item {
                GroupRange(
                    ranges = provisioner.allocatedGroupRanges,
                    otherRanges = otherProvisioners.flatMap { it.allocatedGroupRanges },
                    navigateToRanges = { navigateToGroupRanges(provisioner.uuid) }
                )
            }
            item {
                SceneRange(
                    ranges = provisioner.allocatedSceneRanges,
                    otherRanges = otherProvisioners.flatMap { it.allocatedSceneRanges },
                    navigateToRanges = { navigateToSceneRanges(provisioner.uuid) }
                )
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))
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
    ElevatedCardItemTextField(
        modifier = Modifier.padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.Badge,
        title = stringResource(id = R.string.label_name),
        subtitle = name,
        placeholder = stringResource(id = R.string.label_placeholder_provisioner_name),
        onValueChanged = onNameChanged,
        isEditable = isCurrentlyEditable,
        onEditableStateChanged = onEditableStateChanged
    )
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
private fun UnicastAddress(
    context: Context,
    snackbarHostState: SnackbarHostState,
    keyboardController: SoftwareKeyboardController?,
    address: Address?,
    onAddressChanged: (Int) -> Unit,
    isValidAddress: (UShort) -> Boolean,
    disableConfigurationCapabilities: () -> Unit,
    isCurrentlyEditable: Boolean,
    onEditableStateChanged: () -> Unit,
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

    ElevatedCard(modifier = Modifier.padding(horizontal = 8.dp)) {
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
                                if (isValidAddress(it.text.toUShort(16))) {
                                    error = false
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                } else {
                                    error = true
                                    errorMessage = "Invalid unicast address"
                                }
                            }
                        },
                        label = {
                            Text(text = stringResource(id = R.string.label_unicast_address))
                        },
                        internalTrailingIcon = {
                            IconButton(
                                enabled = value.text.isNotBlank(),
                                onClick = {
                                    value = TextFieldValue("", TextRange(0))
                                    error = false
                                }
                            ) {
                                Icon(imageVector = Icons.Outlined.Clear, contentDescription = null)
                            }
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
                                onClick = { onUnassignClick = !onUnassignClick }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.GppMaybe,
                                    contentDescription = null,
                                    tint = Color.Red.copy(alpha = 0.6f)
                                )
                            }
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
                                            onAddressChanged(value.text.toInt(radix = 16))
                                        }.onSuccess {
                                            error = false
                                            onEditClick = false
                                            onEditableStateChanged()
                                        }.onFailure { t ->
                                            error = true
                                            errorMessage = t.convertToString(context = context)
                                        }
                                    }
                                }
                            ) {
                                Icon(imageVector = Icons.Outlined.Check, contentDescription = null)
                            }
                        }
                    )

                    false -> MeshTwoLineListItem(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        title = stringResource(id = R.string.label_unicast_address),
                        subtitle = address?.let {
                            "0x${it.toHexString()}"
                        } ?: stringResource(id = R.string.label_not_assigned),
                        trailingComposable = {
                            IconButton(
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
        modifier = Modifier.padding(horizontal = 8.dp),
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

@OptIn(ExperimentalStdlibApi::class)
@Composable
private fun DeviceKey(key: ByteArray?) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.VpnKey,
        title = stringResource(id = R.string.label_device_key),
        subtitle = key?.toHexString() ?: stringResource(R.string.label_not_applicable)
    )
}

@Composable
private fun UnicastRange(
    ranges: List<UnicastRange>,
    otherRanges: List<UnicastRange>,
    navigateToRanges: () -> Unit
) {
    ElevatedCard(modifier = Modifier.padding(horizontal = 8.dp)) {
        AllocatedRanges(
            imageVector = Icons.Outlined.Lan,
            title = stringResource(id = R.string.label_unicast_range),
            ranges = ranges,
            otherRanges = otherRanges,
            onClick = navigateToRanges
        )
    }
}

@Composable
private fun GroupRange(
    ranges: List<GroupRange>,
    otherRanges: List<GroupRange>,
    navigateToRanges: () -> Unit
) {
    ElevatedCard(modifier = Modifier.padding(horizontal = 8.dp)) {
        AllocatedRanges(
            imageVector = Icons.Outlined.GroupWork,
            title = stringResource(id = R.string.label_group_range),
            ranges = ranges,
            otherRanges = otherRanges,
            onClick = navigateToRanges
        )
    }
}

@Composable
private fun SceneRange(
    ranges: List<SceneRange>,
    otherRanges: List<SceneRange>,
    navigateToRanges: () -> Unit
) {
    ElevatedCard(modifier = Modifier.padding(horizontal = 8.dp)) {
        AllocatedRanges(
            imageVector = Icons.Outlined.AutoAwesome,
            title = stringResource(id = R.string.label_scene_range),
            ranges = ranges,
            otherRanges = otherRanges,
            onClick = navigateToRanges
        )
    }
}