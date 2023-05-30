@file:OptIn(
    ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class
)

package no.nordicsemi.android.nrfmesh.ui

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lan
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EnhancedEncryption
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.theme.nordicLightGray
import no.nordicsemi.android.nrfmesh.R
import no.nordicsemi.android.nrfmesh.core.ui.MeshAlertDialog
import no.nordicsemi.android.nrfmesh.core.ui.MeshOutlinedTextField
import no.nordicsemi.android.nrfmesh.core.ui.MeshTwoLineListItem
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.core.ui.convertToString
import no.nordicsemi.android.nrfmesh.core.ui.showSnackbar
import no.nordicsemi.android.nrfmesh.viewmodel.ProvisionerState
import no.nordicsemi.android.nrfmesh.viewmodel.ProvisioningViewModel
import no.nordicsemi.kotlin.mesh.core.model.Address
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.toHex
import no.nordicsemi.kotlin.mesh.provisioning.AuthenticationMethod
import no.nordicsemi.kotlin.mesh.provisioning.ProvisioningCapabilities
import no.nordicsemi.kotlin.mesh.provisioning.ProvisioningConfiguration
import no.nordicsemi.kotlin.mesh.provisioning.ProvisioningState
import no.nordicsemi.kotlin.mesh.provisioning.UnprovisionedDevice

@Composable
fun ProvisioningRoute(viewModel: ProvisioningViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BackHandler(
        enabled = uiState.provisionerState is ProvisionerState.Connecting ||
                uiState.provisionerState is ProvisionerState.Connected ||
                uiState.provisionerState is ProvisionerState.Identifying ||
                uiState.provisionerState is ProvisionerState.Provisioning ||
                uiState.provisionerState is ProvisionerState.Disconnected
    ) {
        viewModel.disconnect()
    }
    ProvisioningScreen(
        unprovisionedDevice = uiState.unprovisionedDevice,
        provisionerState = uiState.provisionerState,
        onNameChanged = viewModel::onNameChanged,
        onAddressChanged = viewModel::onAddressChanged,
        isValidAddress = viewModel::isValidAddress,
        onNetworkKeyClick = viewModel::onNetworkKeyClick,
        startProvisioning = viewModel::startProvisioning,
        onProvisioningComplete = viewModel::onProvisioningComplete
    )
}

@Composable
private fun ProvisioningScreen(
    provisionerState: ProvisionerState,
    unprovisionedDevice: UnprovisionedDevice,
    onNameChanged: (String) -> Unit,
    onAddressChanged: (ProvisioningConfiguration, Int, Int) -> Result<Boolean>,
    isValidAddress: (UShort) -> Boolean,
    onNetworkKeyClick: (KeyIndex) -> Unit,
    startProvisioning: (AuthenticationMethod) -> Unit,
    onProvisioningComplete: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    var capabilities by remember { mutableStateOf<ProvisioningCapabilities?>(null) }
    ModalBottomSheetLayout(
        sheetContent = {
            capabilities?.let { it ->
                OobBottomSheet(
                    capabilities = it,
                    onConfirmClicked = {
                        scope.launch {
                            sheetState.hide()
                        }
                        startProvisioning(it)
                    }
                )
            }
        },
        sheetState = sheetState,
        sheetShape = RoundedCornerShape(12.dp),
    ) {
        when (provisionerState) {
            is ProvisionerState.Connecting -> ProvisionerStateInfo(
                text = stringResource(R.string.label_connecting)
            )

            is ProvisionerState.Connected -> ProvisionerStateInfo(
                text = stringResource(R.string.label_connected)
            )

            ProvisionerState.Identifying -> ProvisionerStateInfo(
                text = stringResource(R.string.label_identifying)
            )

            is ProvisionerState.Provisioning -> ProvisioningStateInfo(
                unprovisionedDevice = unprovisionedDevice,
                state = provisionerState.state,
                onNameChanged = onNameChanged,
                onAddressChanged = onAddressChanged,
                isValidAddress = isValidAddress,
                onNetworkKeyClick = onNetworkKeyClick,
                onProvisionClick = {
                    capabilities = it
                    scope.launch { sheetState.show() }
                },
                onProvisioningComplete = onProvisioningComplete
            )

            is ProvisionerState.Disconnected -> ProvisionerStateInfo(
                text = stringResource(R.string.label_disconnected)
            )
        }
    }

}

@Composable
private fun ProvisioningStateInfo(
    state: ProvisioningState,
    unprovisionedDevice: UnprovisionedDevice,
    onNameChanged: (String) -> Unit,
    onAddressChanged: (ProvisioningConfiguration, Int, Int) -> Result<Boolean>,
    isValidAddress: (UShort) -> Boolean,
    onNetworkKeyClick: (KeyIndex) -> Unit,
    onProvisionClick: (ProvisioningCapabilities) -> Unit,
    onProvisioningComplete: () -> Unit,
) {
    when (state) {
        is ProvisioningState.RequestingCapabilities -> {
            ProvisionerStateInfo(text = "Requesting capabilities")
        }

        is ProvisioningState.CapabilitiesReceived -> {
            DeviceCapabilities(
                state = state,
                unprovisionedDevice = unprovisionedDevice,
                onNameChanged = onNameChanged,
                onAddressChanged = onAddressChanged,
                isValidAddress = isValidAddress,
                onNetworkKeyClick = onNetworkKeyClick,
                onProvisionClick = onProvisionClick
            )
        }

        is ProvisioningState.Provisioning ->
            ProvisionerStateInfo(text = stringResource(R.string.provisioning_in_progress))

        is ProvisioningState.AuthActionRequired ->
            ProvisionerStateInfo(text = stringResource(R.string.label_provisioning_authentication_required))

        is ProvisioningState.Failed -> ProvisionerStateInfo(
            text = stringResource(
                R.string.provisioning_failed,
                state.error
            )
        )

        is ProvisioningState.Complete -> {
            var showProvisioningComplete by remember { mutableStateOf(true) }
            if (showProvisioningComplete) MeshAlertDialog(
                onDismissRequest = {
                    showProvisioningComplete = !showProvisioningComplete
                    onProvisioningComplete()
                },
                confirmButtonText = stringResource(id = R.string.label_ok),
                onConfirmClick = {
                    showProvisioningComplete = !showProvisioningComplete
                    onProvisioningComplete()
                },
                dismissButtonText = null,
                icon = Icons.Rounded.CheckCircle,
                iconColor = MaterialTheme.colorScheme.nordicLightGray,
                title = stringResource(R.string.label_status),
                text = stringResource(R.string.label_provisioning_completed)
            )
        }
    }
}

@Composable
private fun ProvisionerStateInfo(text: String, shouldShowProgress: Boolean = true) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (shouldShowProgress) {
            CircularProgressIndicator()
        }
        Spacer(modifier = Modifier.size(16.dp))
        Text(text = text)
    }
}

@Composable
private fun DeviceCapabilities(
    state: ProvisioningState.CapabilitiesReceived,
    unprovisionedDevice: UnprovisionedDevice,
    onNameChanged: (String) -> Unit,
    onAddressChanged: (ProvisioningConfiguration, Int, Int) -> Result<Boolean>,
    isValidAddress: (UShort) -> Boolean,
    onNetworkKeyClick: (KeyIndex) -> Unit,
    onProvisionClick: (ProvisioningCapabilities) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    var isCurrentlyEditable by rememberSaveable { mutableStateOf(true) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Name(name = unprovisionedDevice.name,
                onNameChanged = onNameChanged,
                isCurrentlyEditable = isCurrentlyEditable,
                onEditableStateChanged = { isCurrentlyEditable = !isCurrentlyEditable })
        }
        item { SectionTitle(title = stringResource(R.string.title_provisioning_data)) }
        item {
            UnicastAddressRow(context = context,
                scope = scope,
                snackbarHostState = snackbarHostState,
                keyboardController = keyboardController,
                address = state.configuration.unicastAddress!!.address,
                onAddressChanged = {
                    onAddressChanged(
                        state.configuration, state.capabilities.numberOfElements, it
                    )
                },
                isValidAddress = isValidAddress,
                isCurrentlyEditable = isCurrentlyEditable,
                onEditableStateChanged = { isCurrentlyEditable = !isCurrentlyEditable })
        }
        item {
            KeyRow(
                modifier = Modifier.clickable {
                    onNetworkKeyClick(state.configuration.networkKey.index)
                }, name = state.configuration.networkKey.name
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
            ) {
                Button(onClick = {
                    runCatching {
                        onProvisionClick(state.capabilities)
                        // showOobType = true
                    }.onFailure {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = it.message
                                    ?: context.getString(R.string.label_unknown_error)
                            )
                        }
                    }.onSuccess {

                    }
                }) {
                    Text(text = stringResource(R.string.label_provision))
                }
            }
        }
        item {
            SectionTitle(title = stringResource(R.string.title_device_capabilities))
        }
        item {
            ElementsRow(
                leadingComposable = {
                    Icon(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        imageVector = Icons.Rounded.Badge,
                        contentDescription = null,
                        tint = LocalContentColor.current.copy(alpha = 0.6f)
                    )
                },
                title = stringResource(R.string.label_element_count),
                subtitle = "${state.capabilities.numberOfElements}"
            )
        }
        item {
            SupportedAlgorithmsRow(leadingComposable = {
                Icon(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    imageVector = Icons.Rounded.EnhancedEncryption,
                    contentDescription = null,
                    tint = LocalContentColor.current.copy(alpha = 0.6f)
                )
            },
                title = stringResource(R.string.label_supported_algorithms),
                subtitle = state.capabilities.algorithms.joinToString(separator = ", ")
                    .ifEmpty { "None" })
        }
        item {
            PublicKeyTypeRow(leadingComposable = {
                Icon(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    imageVector = Icons.Rounded.Key,
                    contentDescription = null,
                    tint = LocalContentColor.current.copy(alpha = 0.6f)
                )
            },
                title = stringResource(R.string.label_public_key_type),
                subtitle = state.capabilities.publicKeyType.joinToString(separator = ", ")
                    .ifEmpty { "None" })
        }
        item {
            StaticOobTypeRow(leadingComposable = {
                Icon(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    imageVector = Icons.Rounded.Key,
                    contentDescription = null,
                    tint = LocalContentColor.current.copy(alpha = 0.6f)
                )
            },
                title = stringResource(R.string.label_static_oob_type),
                subtitle = state.capabilities.oobTypes.joinToString(separator = ", ")
                    .ifEmpty { "None" })
        }
        item {
            OutputOobSizeRow(
                leadingComposable = {
                    Icon(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        imageVector = Icons.Rounded.Key,
                        contentDescription = null,
                        tint = LocalContentColor.current.copy(alpha = 0.6f)
                    )
                },
                title = stringResource(R.string.label_output_oob_size),
                subtitle = "${state.capabilities.outputOobSize}"
            )
        }
        item {
            OutputOobActionsRow(leadingComposable = {
                Icon(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    imageVector = Icons.Rounded.Key,
                    contentDescription = null,
                    tint = LocalContentColor.current.copy(alpha = 0.6f)
                )
            },
                title = stringResource(R.string.label_output_oob_actions),
                subtitle = state.capabilities.outputOobActions.joinToString(separator = ", ")
                    .ifEmpty { "None" })
        }
        item {
            InputOobSizeRow(
                leadingComposable = {
                    Icon(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        imageVector = Icons.Rounded.Key,
                        contentDescription = null,
                        tint = LocalContentColor.current.copy(alpha = 0.6f)
                    )
                },
                title = stringResource(R.string.label_input_oob_size),
                subtitle = "${state.capabilities.inputOobSize}"
            )
        }
        item {
            InputOobActionsRow(leadingComposable = {
                Icon(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    imageVector = Icons.Rounded.Key,
                    contentDescription = null,
                    tint = LocalContentColor.current.copy(alpha = 0.6f)
                )
            },
                title = stringResource(R.string.label_input_oob_actions),
                subtitle = state.capabilities.inputOobActions.joinToString(separator = ", ")
                    .ifBlank { "None" })
        }
    }
}

@Composable
private fun Name(
    name: String,
    onNameChanged: (String) -> Unit,
    isCurrentlyEditable: Boolean,
    onEditableStateChanged: () -> Unit,
) {
    var value by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = name, selection = TextRange(name.length)))
    }
    var onEditClick by rememberSaveable { mutableStateOf(false) }
    Crossfade(targetState = onEditClick) { state ->
        when (state) {
            true -> MeshOutlinedTextField(modifier = Modifier.padding(vertical = 8.dp),
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
                label = { Text(text = stringResource(id = no.nordicsemi.android.feature.provisioners.R.string.label_name)) },
                placeholder = { Text(text = stringResource(id = no.nordicsemi.android.feature.provisioners.R.string.label_placeholder_provisioner_name)) },
                internalTrailingIcon = {
                    IconButton(enabled = value.text.isNotBlank(), onClick = {
                        value = TextFieldValue(text = "", selection = TextRange(0))
                    }) { Icon(imageVector = Icons.Outlined.Clear, contentDescription = null) }
                },
                content = {
                    IconButton(modifier = Modifier.padding(start = 8.dp, end = 16.dp),
                        enabled = value.text.isNotBlank(),
                        onClick = {
                            onEditClick = !onEditClick
                            onEditableStateChanged()
                            onNameChanged(value.text.trim())
                        }) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            tint = LocalContentColor.current.copy(alpha = 0.6f)
                        )
                    }
                })

            false -> MeshTwoLineListItem(leadingComposable = {
                Icon(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    imageVector = Icons.Outlined.Badge,
                    contentDescription = null,
                    tint = LocalContentColor.current.copy(alpha = 0.6f)
                )
            },
                title = stringResource(id = no.nordicsemi.android.feature.provisioners.R.string.label_name),
                subtitle = value.text,
                trailingComposable = {
                    IconButton(modifier = Modifier.padding(horizontal = 16.dp),
                        enabled = isCurrentlyEditable,
                        onClick = {
                            onEditClick = !onEditClick
                            onEditableStateChanged()
                        }) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            tint = LocalContentColor.current.copy(alpha = 0.6f)
                        )
                    }
                })
        }
    }
}

@Composable
private fun UnicastAddressRow(
    context: Context,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    keyboardController: SoftwareKeyboardController?,
    address: Address = UnicastAddress(1u).address,
    onAddressChanged: (Int) -> Result<Boolean>,
    isValidAddress: (UShort) -> Boolean,
    isCurrentlyEditable: Boolean,
    onEditableStateChanged: () -> Unit,
) {
    val tempAddress by remember { mutableStateOf(address.toHex()) }
    var value by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(text = tempAddress, selection = TextRange(tempAddress.length))
        )
    }
    var error by rememberSaveable { mutableStateOf(false) }
    var onEditClick by rememberSaveable { mutableStateOf(false) }
    var supportingErrorText by rememberSaveable { mutableStateOf("") }
    Crossfade(targetState = onEditClick) { state ->
        when (state) {
            true -> MeshOutlinedTextField(modifier = Modifier.padding(vertical = 8.dp),
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
                    if (it.text.isNotEmpty()) {
                        runCatching {
                            error = !isValidAddress(it.text.toUShort(16))
                        }.onFailure { throwable ->
                            supportingErrorText = throwable.message ?: ""
                            error = true
                        }
                    }
                },
                label = {
                    Text(
                        text = stringResource(id = R.string.label_unicast_address)
                    )
                },
                internalTrailingIcon = {
                    IconButton(enabled = value.text.isNotBlank(), onClick = {
                        value = TextFieldValue("", TextRange(0))
                        error = false
                    }) { Icon(imageVector = Icons.Outlined.Clear, contentDescription = null) }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters
                ),
                regex = Regex("[0-9A-Fa-f]{0,4}"),
                isError = error,
                supportingText = {
                    if (error) Text(
                        text = supportingErrorText,
                        color = MaterialTheme.colorScheme.error
                    )
                },
                content = {
                    IconButton(modifier = Modifier.padding(end = 16.dp),
                        enabled = value.text.isNotEmpty(),
                        onClick = {
                            keyboardController?.hide()
                            if (value.text.isNotEmpty()) {
                                onAddressChanged(value.text.toInt(radix = 16)).onSuccess {
                                    if (it) {
                                        onEditClick = !onEditClick
                                        onEditableStateChanged()
                                    } else {
                                        error = true
                                        showSnackbar(
                                            scope = scope,
                                            snackbarHostState = snackbarHostState,
                                            message = context.getString(R.string.error_invalid_address)
                                        )
                                    }
                                }.onFailure {
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
                        }) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            tint = LocalContentColor.current.copy(alpha = 0.6f)
                        )
                    }
                })

            false -> MeshTwoLineListItem(leadingComposable = {
                Icon(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    imageVector = Icons.Outlined.Lan,
                    contentDescription = null,
                    tint = LocalContentColor.current.copy(alpha = 0.6f)
                )
            },
                title = stringResource(id = no.nordicsemi.android.feature.provisioners.R.string.label_unicast_address),
                subtitle = address.toHex(prefix0x = true),
                trailingComposable = {
                    IconButton(modifier = Modifier.padding(horizontal = 16.dp),
                        enabled = isCurrentlyEditable,
                        onClick = {
                            error = false
                            onEditClick = !onEditClick
                            onEditableStateChanged()
                        }) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            tint = LocalContentColor.current.copy(alpha = 0.6f)
                        )
                    }
                })
        }
    }
}

@Composable
private fun KeyRow(modifier: Modifier, name: String) {
    MeshTwoLineListItem(
        modifier = modifier, leadingComposable = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.VpnKey,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        }, title = stringResource(R.string.title_network_key), subtitle = name
    )
}

@Composable
private fun ElementsRow(
    modifier: Modifier = Modifier,
    leadingComposable: @Composable () -> Unit,
    title: String,
    subtitle: String
) {
    MeshTwoLineListItem(
        modifier = modifier,
        leadingComposable = leadingComposable,
        title = title,
        subtitle = subtitle
    )
}

@Composable
private fun SupportedAlgorithmsRow(
    modifier: Modifier = Modifier,
    leadingComposable: @Composable () -> Unit,
    title: String,
    subtitle: String
) {
    MeshTwoLineListItem(
        modifier = modifier,
        leadingComposable = leadingComposable,
        title = title,
        subtitle = subtitle
    )
}

@Composable
private fun PublicKeyTypeRow(
    modifier: Modifier = Modifier,
    leadingComposable: @Composable () -> Unit,
    title: String,
    subtitle: String
) {
    MeshTwoLineListItem(
        modifier = modifier,
        leadingComposable = leadingComposable,
        title = title,
        subtitle = subtitle
    )
}

@Composable
private fun StaticOobTypeRow(
    modifier: Modifier = Modifier,
    leadingComposable: @Composable () -> Unit,
    title: String,
    subtitle: String
) {
    MeshTwoLineListItem(
        modifier = modifier,
        leadingComposable = leadingComposable,
        title = title,
        subtitle = subtitle
    )
}

@Composable
private fun OutputOobSizeRow(
    modifier: Modifier = Modifier,
    leadingComposable: @Composable () -> Unit,
    title: String,
    subtitle: String
) {
    MeshTwoLineListItem(
        modifier = modifier,
        leadingComposable = leadingComposable,
        title = title,
        subtitle = subtitle
    )
}

@Composable
private fun OutputOobActionsRow(
    modifier: Modifier = Modifier,
    leadingComposable: @Composable () -> Unit,
    title: String,
    subtitle: String
) {
    MeshTwoLineListItem(
        modifier = modifier,
        leadingComposable = leadingComposable,
        title = title,
        subtitle = subtitle
    )
}

@Composable
private fun InputOobSizeRow(
    modifier: Modifier = Modifier,
    leadingComposable: @Composable () -> Unit,
    title: String,
    subtitle: String
) {
    MeshTwoLineListItem(
        modifier = modifier,
        leadingComposable = leadingComposable,
        title = title,
        subtitle = subtitle
    )
}

@Composable
private fun InputOobActionsRow(
    modifier: Modifier = Modifier,
    leadingComposable: @Composable () -> Unit,
    title: String,
    subtitle: String
) {
    MeshTwoLineListItem(
        modifier = modifier,
        leadingComposable = leadingComposable,
        title = title,
        subtitle = subtitle
    )
}