package no.nordicsemi.android.nrfmesh.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.nrfmesh.R
import no.nordicsemi.android.nrfmesh.core.ui.MeshOutlinedTextField
import no.nordicsemi.kotlin.mesh.provisioning.AuthenticationMethod
import no.nordicsemi.kotlin.mesh.provisioning.InputAction.Companion.toInputActions
import no.nordicsemi.kotlin.mesh.provisioning.OutputAction.Companion.toOutputActions
import no.nordicsemi.kotlin.mesh.provisioning.ProvisioningCapabilities

@Composable
internal fun OobBottomSheet(
    capabilities: ProvisioningCapabilities,
    onConfirmClicked: (AuthenticationMethod) -> Unit
) {
    var selectedIndex by rememberSaveable { mutableStateOf(-1) }
    var selectedActionIndex by rememberSaveable { mutableStateOf(-1) }
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.label_select_oob_type_to_use),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                capabilities.supportedAuthMethods.forEachIndexed { index, auth ->
                    RadioButtonRow(
                        text = auth.description(),
                        selectedIndex = selectedIndex,
                        index = index,
                        onClick = {
                            selectedActionIndex = -1
                            selectedIndex = index
                        }
                    )
                    AnimatedVisibility(visible = selectedIndex == index) {
                        Column(modifier = Modifier.padding(start = 32.dp)) {
                            when (auth) {
                                is AuthenticationMethod.StaticOob -> {
                                    MeshOutlinedTextField(value = "", onValueChanged = {})
                                }

                                is AuthenticationMethod.OutputOob -> {
                                    capabilities.outputOobActions.forEachIndexed { index, action ->
                                        RadioButtonRow(
                                            text = action.toString(),
                                            selectedIndex = selectedActionIndex,
                                            index = index,
                                            onClick = { selectedActionIndex = index }
                                        )
                                    }
                                }

                                is AuthenticationMethod.InputOob -> {
                                    capabilities.inputOobActions.forEachIndexed { index, action ->
                                        RadioButtonRow(
                                            text = action.toString(),
                                            selectedIndex = selectedActionIndex,
                                            index = index,
                                            onClick = { selectedActionIndex = index }
                                        )
                                    }
                                }

                                else -> {}
                            }
                        }
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Button(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .defaultMinSize(minWidth = 120.dp),
                    enabled = isEnabled(capabilities, selectedIndex, selectedActionIndex),
                    onClick = {
                        onConfirmClicked(
                            selectedOob(
                                capabilities = capabilities,
                                selectedIndex = selectedIndex,
                                selectedActionIndex = selectedActionIndex
                            )
                        )
                    },
                    content = { Text(text = stringResource(id = R.string.label_ok)) }
                )
            }
        }
    }
}

@Composable
private fun RadioButtonRow(text: String, selectedIndex: Int, index: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selectedIndex == index,
            onClick = onClick
        )
        Text(modifier = Modifier.padding(horizontal = 16.dp), text = text)
    }
}

@Composable
private fun AuthenticationMethod.description(): String = when (this) {
    is AuthenticationMethod.NoOob -> stringResource(
        id = R.string.label_no_oob
    )

    is AuthenticationMethod.StaticOob -> stringResource(
        id = R.string.label_static_oob
    )

    is AuthenticationMethod.OutputOob -> stringResource(
        id = R.string.label_output_oob
    )

    is AuthenticationMethod.InputOob -> stringResource(
        id = R.string.label_input_oob
    )
}

private fun isEnabled(
    capabilities: ProvisioningCapabilities,
    selectedIndex: Int,
    selectedActionIndex: Int
): Boolean = when (capabilities.supportedAuthMethods[selectedIndex]) {
    is AuthenticationMethod.NoOob, AuthenticationMethod.StaticOob -> {
        true
    }

    is AuthenticationMethod.OutputOob, is AuthenticationMethod.InputOob -> {
        selectedActionIndex != -1
    }
}

private fun selectedOob(
    capabilities: ProvisioningCapabilities,
    selectedIndex: Int,
    selectedActionIndex: Int
): AuthenticationMethod = when (capabilities.supportedAuthMethods[selectedIndex]) {
    is AuthenticationMethod.StaticOob -> {
        AuthenticationMethod.StaticOob
    }

    is AuthenticationMethod.OutputOob -> {
        AuthenticationMethod.OutputOob(
            action = capabilities.outputOobActions
                .toOutputActions()[selectedActionIndex],
            length = capabilities.outputOobSize
        )
    }

    is AuthenticationMethod.InputOob -> {
        AuthenticationMethod.InputOob(
            action = capabilities.inputOobActions
                .toInputActions()[selectedActionIndex],
            length = capabilities.inputOobSize
        )
    }

    is AuthenticationMethod.NoOob -> {
        AuthenticationMethod.NoOob
    }
}