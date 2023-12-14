@file:OptIn(ExperimentalComposeUiApi::class)

package no.nordicsemi.android.nrfmesh.ui.provisioning

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lan
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.EnhancedEncryption
import androidx.compose.material.icons.rounded.Key
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
import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.android.nrfmesh.R
import no.nordicsemi.android.nrfmesh.core.ui.MeshOutlinedTextField
import no.nordicsemi.android.nrfmesh.core.ui.MeshTwoLineListItem
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.core.common.convertToString
import no.nordicsemi.android.nrfmesh.core.ui.showSnackbar
import no.nordicsemi.kotlin.mesh.core.model.Address
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.toHex
import no.nordicsemi.kotlin.mesh.provisioning.AuthenticationMethod
import no.nordicsemi.kotlin.mesh.provisioning.ProvisioningParameters
import no.nordicsemi.kotlin.mesh.provisioning.ProvisioningState
import no.nordicsemi.kotlin.mesh.provisioning.UnprovisionedDevice


@Composable
internal fun DeviceCapabilities(
    state: ProvisioningState.CapabilitiesReceived,
    snackbarHostState: SnackbarHostState,
    unprovisionedDevice: UnprovisionedDevice,
    showAuthenticationDialog: Boolean,
    onAuthenticationDialogDismissed: (Boolean) -> Unit,
    onNameChanged: (String) -> Unit,
    onAddressChanged: (ProvisioningParameters, Int, Int) -> Result<Boolean>,
    isValidAddress: (UShort) -> Boolean,
    onNetworkKeyClick: (KeyIndex) -> Unit,
    startProvisioning: (AuthenticationMethod) -> Unit,
) {
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
                address = state.parameters.unicastAddress!!.address,
                onAddressChanged = {
                    onAddressChanged(
                        state.parameters, state.capabilities.numberOfElements, it
                    )
                },
                isValidAddress = isValidAddress,
                isCurrentlyEditable = isCurrentlyEditable,
                onEditableStateChanged = { isCurrentlyEditable = !isCurrentlyEditable })
        }
        item {
            KeyRow(
                modifier = Modifier.clickable {
                    onNetworkKeyClick(state.parameters.networkKey.index)
                }, name = state.parameters.networkKey.name
            )
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
            SupportedAlgorithmsRow(
                leadingComposable = {
                    Icon(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        imageVector = Icons.Rounded.EnhancedEncryption,
                        contentDescription = null,
                        tint = LocalContentColor.current.copy(alpha = 0.6f)
                    )
                },
                title = stringResource(R.string.label_supported_algorithms),
                subtitle = state.capabilities.algorithms.joinToString(separator = ", ")
                    .ifEmpty { "None" }
            )
        }
        item {
            PublicKeyTypeRow(
                leadingComposable = {
                    Icon(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        imageVector = Icons.Rounded.Key,
                        contentDescription = null,
                        tint = LocalContentColor.current.copy(alpha = 0.6f)
                    )
                },
                title = stringResource(R.string.label_public_key_type),
                subtitle = state.capabilities.publicKeyType.joinToString(separator = ", ")
                    .ifEmpty { "None" }
            )
        }
        item {
            StaticOobTypeRow(
                leadingComposable = {
                    Icon(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        imageVector = Icons.Rounded.Key,
                        contentDescription = null,
                        tint = LocalContentColor.current.copy(alpha = 0.6f)
                    )
                },
                title = stringResource(R.string.label_static_oob_type),
                subtitle = state.capabilities.oobTypes.joinToString(separator = ", ")
                    .ifEmpty { "None" }
            )
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
            OutputOobActionsRow(
                leadingComposable = {
                    Icon(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        imageVector = Icons.Rounded.Key,
                        contentDescription = null,
                        tint = LocalContentColor.current.copy(alpha = 0.6f)
                    )
                },
                title = stringResource(R.string.label_output_oob_actions),
                subtitle = state.capabilities.outputOobActions.joinToString(separator = ", ")
                    .ifEmpty { "None" }
            )
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
            InputOobActionsRow(
                leadingComposable = {
                    Icon(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        imageVector = Icons.Rounded.Key,
                        contentDescription = null,
                        tint = LocalContentColor.current.copy(alpha = 0.6f)
                    )
                },
                title = stringResource(R.string.label_input_oob_actions),
                subtitle = state.capabilities.inputOobActions.joinToString(separator = ", ")
                    .ifBlank { "None" }
            )
        }
    }

    if (showAuthenticationDialog) {
        AuthSelectionBottomSheet(
            capabilities = state.capabilities,
            onConfirmClicked = { startProvisioning(it) },
            onDismissRequest = { onAuthenticationDialogDismissed(false) },
        )
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
    Crossfade(targetState = onEditClick, label = "Name") { state ->
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
    Crossfade(targetState = onEditClick, label = "UnicastAddress") { state ->
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