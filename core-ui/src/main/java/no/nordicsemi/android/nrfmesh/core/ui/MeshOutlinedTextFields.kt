@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun MeshOutlinedTextField(
    modifier: Modifier = Modifier,
    onFocus: Boolean = false,
    externalLeadingIcon: @Composable () -> Unit = {},
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    value: String,
    onValueChanged: (String) -> Unit,
    internalTrailingIcon: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    regex: Regex? = null,
    isError: Boolean = regex != null && !regex.matches(value),
    content: @Composable () -> Unit = {},
) {
    val requester = remember { FocusRequester() }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        externalLeadingIcon()
        OutlinedTextField(
            modifier = Modifier
                .weight(1f)
                .focusRequester(requester),
            value = value,
            onValueChange = {
                if (regex == null) {
                    onValueChanged(it)
                } else if (regex.matches(it)) {
                    onValueChanged(value)
                }
            },
            label = label,
            placeholder = placeholder,
            trailingIcon = internalTrailingIcon,
            readOnly = readOnly,
            isError = isError,
            singleLine = true,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )
        content()
    }
    SideEffect {
        if (onFocus) {
            requester.requestFocus()
        }
    }
}

@Composable
fun MeshOutlinedTextField(
    modifier: Modifier = Modifier,
    onFocus: Boolean = false,
    externalLeadingIcon: @Composable () -> Unit = {},
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    value: TextFieldValue,
    onValueChanged: (TextFieldValue) -> Unit,
    internalTrailingIcon: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    regex: Regex? = null,
    isError: Boolean = regex != null && !regex.matches(value.text),
    supportingText: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit = {}
) {
    val requester = remember { FocusRequester() }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        externalLeadingIcon()
        OutlinedTextField(
            modifier = Modifier
                .weight(1f)
                .focusRequester(requester),
            value = value,
            onValueChange = {
                if (regex == null) {
                    onValueChanged(it)
                } else if (regex.matches(it.text)) {
                    onValueChanged(it)
                }
            },
            label = label,
            placeholder = placeholder,
            trailingIcon = internalTrailingIcon,
            readOnly = readOnly,
            isError = isError,
            supportingText = supportingText,
            singleLine = true,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )
        content()
    }
    SideEffect {
        if (onFocus) {
            requester.requestFocus()
        }
    }
}

@Composable
fun MeshOutlinedHexTextField(
    modifier: Modifier = Modifier,
    showPrefix: Boolean = true,
    onFocus: Boolean = false,
    externalLeadingIcon: @Composable () -> Unit = {},
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    value: TextFieldValue,
    onValueChanged: (TextFieldValue) -> Unit,
    internalTrailingIcon: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    regex: Regex? = null,
    isError: Boolean = regex != null && !regex.matches(value.text),
    supportingText: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit = {}
) {
    Row {
        Column(
            modifier = Modifier.height(TextFieldDefaults.MinHeight),
            verticalArrangement = Arrangement.Center
        ) {
            if (showPrefix)
                Text(
                    modifier = Modifier
                        .padding(end = 8.dp),
                    text = "0x"
                )
        }
        MeshOutlinedTextField(
            modifier = modifier,
            onFocus = onFocus,
            externalLeadingIcon = externalLeadingIcon,
            label = label,
            placeholder = placeholder,
            value = value,
            onValueChanged = onValueChanged,
            internalTrailingIcon = internalTrailingIcon,
            readOnly = readOnly,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            regex = regex,
            isError = isError,
            supportingText = supportingText,
            content = content
        )
    }
}