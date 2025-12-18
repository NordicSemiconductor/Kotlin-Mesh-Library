package no.nordicsemi.android.nrfmesh.feature.model.vendor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Segment
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.core.data.meshnetwork.vendor.AcknowledgedVendorMessageImpl
import no.nordicsemi.android.nrfmesh.core.data.meshnetwork.vendor.UnacknowledgedVendorMessageImpl
import no.nordicsemi.android.nrfmesh.core.ui.MeshOutlinedHexTextField
import no.nordicsemi.android.nrfmesh.core.ui.MeshSingleLineListItem
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.feature.models.R
import no.nordicsemi.kotlin.mesh.core.messages.MeshMessage
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.VendorModelId
import no.nordicsemi.kotlin.data.toByteArray
import no.nordicsemi.kotlin.mesh.core.messages.MeshMessageSecurity

@Composable
internal fun VendorModelControls(
    model: Model,
    messageState: MessageState,
    sendApplicationMessage: (Model, MeshMessage) -> Unit,
) {
    Request(
        model = model,
        messageState = messageState,
        sendApplicationMessage = sendApplicationMessage
    )
    Response(messageState = messageState)
}

@Composable
private fun Request(
    model: Model,
    messageState: MessageState,
    sendApplicationMessage: (Model, MeshMessage) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var isOpCodeFocused by rememberSaveable { mutableStateOf(false) }
    var isParametersFocused by rememberSaveable { mutableStateOf(false) }
    var isResponseOpCodeFocused by rememberSaveable { mutableStateOf(false) }
    var opCode by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = ""))
    }
    var isOpCodeValid by rememberSaveable { mutableStateOf(true) }
    var opCodeError by rememberSaveable { mutableStateOf("") }
    var parameters by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = ""))
    }
    var responseOpCode by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = ""))
    }
    var isResponseOpCodeValid by rememberSaveable { mutableStateOf(true) }
    var responseOpCodeError by rememberSaveable { mutableStateOf("") }
    var acknowledged by rememberSaveable { mutableStateOf(false) }
    var sixtyFourBitTransmic by rememberSaveable { mutableStateOf(false) }
    var forceSegmentation by rememberSaveable { mutableStateOf(false) }
    SectionTitle(title = stringResource(R.string.label_request))
    OutlinedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
        Column(
            modifier = Modifier
                .padding(all = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
            ) {
                Text(
                    modifier = Modifier.align(alignment = Alignment.CenterVertically),
                    text = stringResource(R.string.label_vendor_opcode_prefix)
                )
                Text(
                    modifier = Modifier.align(alignment = Alignment.CenterVertically),
                    text = stringResource(R.string.label_or)
                )
                MeshOutlinedHexTextField(
                    modifier = Modifier
                        .clickable {
                            isOpCodeFocused = true
                            isParametersFocused = false
                        },
                    onFocus = isOpCodeFocused,
                    showPrefix = true,
                    value = opCode,
                    onValueChanged = {
                        opCode = it
                        if (it.text.isNotEmpty()) {
                            runCatching {
                                opCodeError = it.text.toUByte(radix = 16).let { code ->
                                    if (code > 0x3Fu) {
                                        isOpCodeValid = false
                                        "Invalid Op Code. Valid range 0x00 - 0x3F"
                                    } else {
                                        isOpCodeValid = true
                                        ""
                                    }
                                }
                            }.onFailure { _ ->
                                opCodeError = "Invalid Op Code. Valid range 0x00 - 0x3F"
                            }
                        }
                    },
                    keyboardActions = KeyboardActions(
                        onDone = {
                            isOpCodeFocused = false
                            isParametersFocused = true
                        }
                    ),
                    isError = !isOpCodeValid,
                    label = { Text(text = stringResource(R.string.label_6_bit_op_code)) },
                    supportingText = {
                        if (opCodeError.isNotEmpty()) {
                            Text(
                                text = opCodeError,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    regex = Regex("^[0-9A-Fa-f]?$"),
                )
            }
            MeshOutlinedHexTextField(
                modifier = Modifier
                    .clickable {
                        isOpCodeFocused = false
                        isParametersFocused = true
                    },
                onFocus = isParametersFocused,
                showPrefix = true,
                value = parameters,
                onValueChanged = { parameters = it },
                keyboardActions = KeyboardActions(
                    onDone = {
                        isParametersFocused = false
                        keyboardController?.hide()
                    }
                ),
                label = { Text(text = stringResource(R.string.label_parameters)) },
                regex = Regex("[0-9A-Fa-f]"),
            )
            MeshSingleLineListItem(
                modifier = Modifier.fillMaxSize(),
                leadingComposable = {
                    Icon(
                        modifier = Modifier.padding(end = 16.dp),
                        imageVector = Icons.Outlined.Verified,
                        contentDescription = null,
                        tint = LocalContentColor.current.copy(alpha = 0.6f)
                    )
                },
                title = stringResource(R.string.label_acknowledged),
                trailingComposable = {
                    Switch(
                        enabled = !messageState.isInProgress(),
                        checked = acknowledged,
                        onCheckedChange = { acknowledged = it },
                    )
                }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
            ) {
                Text(
                    modifier = Modifier.align(alignment = Alignment.CenterVertically),
                    text = stringResource(R.string.label_vendor_opcode_prefix)
                )
                Text(
                    modifier = Modifier.align(alignment = Alignment.CenterVertically),
                    text = stringResource(R.string.label_or)
                )
                MeshOutlinedHexTextField(
                    modifier = Modifier
                        .clickable {
                            isOpCodeFocused = false
                            isParametersFocused = false
                            isResponseOpCodeFocused = true
                        },
                    enabled = acknowledged,
                    onFocus = isResponseOpCodeFocused,
                    showPrefix = true,
                    value = responseOpCode,
                    onValueChanged = {
                        responseOpCode = it
                        if (it.text.isNotEmpty()) {
                            if (it.text.isNotEmpty()) {
                                runCatching {
                                    responseOpCodeError = it.text.toUByte(radix = 16).let { code ->
                                        if (code > 0x3Fu) {
                                            isResponseOpCodeValid = false
                                            "Invalid Op Code. Valid range 0x00 - 0x3F"
                                        } else {
                                            isResponseOpCodeValid = true
                                            ""
                                        }
                                    }
                                }.onFailure { _ ->
                                    opCodeError = "Invalid Op Code. Valid range 0x00 - 0x3F"
                                }
                            }
                        }
                    },
                    keyboardActions = KeyboardActions(
                        onDone = {
                            isResponseOpCodeFocused = false
                            keyboardController?.hide()
                        }
                    ),
                    isError = !isResponseOpCodeValid,
                    label = { Text(text = stringResource(R.string.label_6_bit_response_op_code)) },
                    supportingText = {
                        if (responseOpCodeError.isNotEmpty()) {
                            Text(
                                text = responseOpCodeError,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    regex = Regex("^[0-9A-Fa-f]?$"),
                )
            }
            MeshSingleLineListItem(
                modifier = Modifier.fillMaxSize(),
                leadingComposable = {
                    Icon(
                        modifier = Modifier
                            .padding(end = 16.dp),
                        imageVector = Icons.Outlined.Security,
                        contentDescription = null,
                        tint = LocalContentColor.current.copy(alpha = 0.6f)
                    )
                },
                title = stringResource(R.string.label_64_bit_transmic),
                trailingComposable = {
                    Switch(
                        enabled = !messageState.isInProgress(),
                        checked = sixtyFourBitTransmic,
                        onCheckedChange = { sixtyFourBitTransmic = it },
                    )
                }
            )
            MeshSingleLineListItem(
                modifier = Modifier.fillMaxSize(),
                leadingComposable = {
                    Icon(
                        modifier = Modifier
                            .padding(end = 16.dp),
                        imageVector = Icons.AutoMirrored.Outlined.Segment,
                        contentDescription = null,
                        tint = LocalContentColor.current.copy(alpha = 0.6f)
                    )
                },
                title = stringResource(R.string.label_force_segmentation),
                trailingComposable = {
                    Switch(
                        enabled = !messageState.isInProgress(),
                        checked = forceSegmentation,
                        onCheckedChange = { forceSegmentation = it },
                    )
                }
            )
            OutlinedButton(
                modifier = Modifier.align(alignment = Alignment.End),
                onClick = {
                    val message = if (acknowledged) {
                        AcknowledgedVendorMessageImpl(
                            modelId = model.modelId as VendorModelId,
                            vendorOpCode = opCode.text.toUByte(radix = 16),
                            parameters = if (parameters.text.isNotEmpty()) {
                                parameters.text.toByteArray()
                            } else {
                                null
                            },
                            vendorResponseOpCode = responseOpCode.text.toUByte(radix = 16),
                            isSegmented = forceSegmentation,
                            security = when (sixtyFourBitTransmic) {
                                true -> MeshMessageSecurity.High
                                false -> MeshMessageSecurity.Low
                            }
                        )
                    } else {
                        UnacknowledgedVendorMessageImpl(
                            modelId = model.modelId as VendorModelId,
                            vendorOpCode = opCode.text.toUByte(radix = 16),
                            parameters = if (parameters.text.isNotEmpty()) {
                                parameters.text.toByteArray()
                            } else {
                                null
                            },
                            isSegmented = forceSegmentation,
                            security = when (sixtyFourBitTransmic) {
                                true -> MeshMessageSecurity.High
                                false -> MeshMessageSecurity.Low
                            }
                        )
                    }
                    sendApplicationMessage(model, message)
                },
                enabled = !messageState.isInProgress() && if (acknowledged) {
                    isOpCodeValid &&
                            isResponseOpCodeValid &&
                            opCode.text.isNotEmpty() &&
                            responseOpCode.text.isNotEmpty()
                } else {
                    isOpCodeValid && opCode.text.isNotEmpty()
                },
                content = { Text(text = stringResource(R.string.label_send)) }
            )
        }
    }
}

@Composable
private fun Response(messageState: MessageState) {
    SectionTitle(title = stringResource(R.string.label_response))
    OutlinedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                MeshSingleLineListItem(
                    modifier = Modifier.weight(weight = 1f),
                    leadingComposable = {
                        Icon(
                            modifier = Modifier
                                .padding(end = 16.dp),
                            imageVector = Icons.Outlined.Code,
                            contentDescription = null,
                            tint = LocalContentColor.current.copy(alpha = 0.6f)
                        )
                    },
                    title = stringResource(R.string.label_op_code)
                )
                Text(
                    text = when (messageState.message) {
                        is AcknowledgedVendorMessageImpl -> messageState
                            .response
                            ?.opCode
                            ?.toHexString(
                                format = HexFormat {
                                    number {
                                        prefix = "0x"
                                        removeLeadingZeros = true
                                    }
                                    upperCase = true
                                }
                            ) ?: ""

                        else -> ""
                    }
                )
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                MeshSingleLineListItem(
                    modifier = Modifier.weight(weight = 1f),
                    leadingComposable = {
                        Icon(
                            modifier = Modifier
                                .padding(end = 16.dp),
                            imageVector = Icons.Outlined.Verified,
                            contentDescription = null,
                            tint = LocalContentColor.current.copy(alpha = 0.6f)
                        )
                    },
                    title = stringResource(R.string.label_status)
                )
                Text(
                    text = when (messageState.message) {
                        is AcknowledgedVendorMessageImpl -> messageState
                            .response
                            ?.parameters
                            ?.toHexString(
                                format = HexFormat {
                                    number {
                                        prefix = "0x"
                                        removeLeadingZeros = true
                                    }
                                    upperCase = true
                                }
                            ) ?: ""

                        else -> ""
                    }
                )
            }
        }
    }
}