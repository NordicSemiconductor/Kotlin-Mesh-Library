@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun MeshOutlinedTextField(
    modifier: Modifier = Modifier,
    onFocus: Boolean = false,
    leadingComposable: @Composable () -> Unit = {},
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
    supportingText: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit = {},
) {
    val requester = remember { FocusRequester() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height = 80.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leadingComposable()
        OutlinedTextField(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
                .focusRequester(requester),
            value = TextFieldValue(text = value, selection = TextRange(value.length)),
            onValueChange = {
                if (regex == null) {
                    onValueChanged(it.text)
                } else if (regex.matches(it.text)) {
                    onValueChanged(it.text)
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
        modifier = modifier
            .fillMaxWidth()
            .height(height = 80.dp),
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
        if (onFocus) requester.requestFocus()
    }
}

@Composable
fun MeshOutlinedHexTextField(
    modifier: Modifier = Modifier,
    showPrefix: Boolean = true,
    onFocus: Boolean = false,
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
                    text = stringResource(R.string.label_hex_prefix)
                )
        }
        MeshOutlinedTextField(
            modifier = modifier,
            onFocus = onFocus,
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