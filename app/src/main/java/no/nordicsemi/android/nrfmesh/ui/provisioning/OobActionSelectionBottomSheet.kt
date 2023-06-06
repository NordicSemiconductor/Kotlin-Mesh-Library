package no.nordicsemi.android.nrfmesh.ui.provisioning

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.nrfmesh.R
import no.nordicsemi.android.nrfmesh.core.ui.MeshOutlinedTextField
import no.nordicsemi.kotlin.mesh.provisioning.AuthAction

@Composable
fun OobActionSelectionBottomSheet(action: AuthAction, onOkClicked: (AuthAction, String) -> Unit) {
    var authValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = ""))
    }
    Surface(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(weight = 1f, fill = false)
            ) {
                Text(
                    text = stringResource(R.string.label_authentication_required),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.size(size = 16.dp))
                AuthRow(action = action, authValue = authValue, onValueChanged = {
                    authValue = it
                })
                Spacer(modifier = Modifier.size(size = 16.dp))
            }
            if (action !is AuthAction.DisplayNumber && action !is AuthAction.DisplayAlphaNumeric) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .defaultMinSize(minWidth = 120.dp),
                        enabled = shouldEnable(action, authValue.text),
                        onClick = { onOkClicked(action, authValue.text) },
                        content = { Text(text = stringResource(id = R.string.label_ok)) }
                    )
                }
            }
        }
    }
}

private fun shouldEnable(action: AuthAction, authValue: String): Boolean {
    return when (action) {
        is AuthAction.DisplayAlphaNumeric,
        is AuthAction.DisplayNumber -> true

        is AuthAction.ProvideAlphaNumeric -> authValue.length <= action.maxNumberOfCharacters.toInt()
        is AuthAction.ProvideNumeric -> authValue.length <= action.maxNumberOfDigits.toInt()
        is AuthAction.ProvideStaticKey -> authValue.length == 32
    }
}

@Composable
private fun AuthRow(
    action: AuthAction,
    authValue: TextFieldValue,
    onValueChanged: (TextFieldValue) -> Unit
) {
    val context = LocalContext.current
    when (action) {
        is AuthAction.DisplayAlphaNumeric -> DisplayAlphaNumeric(action = action)

        is AuthAction.DisplayNumber -> DisplayNumber(action = action)

        is AuthAction.ProvideAlphaNumeric -> ProvideAlphaNumeric(
            context = context,
            authValue = authValue,
            action = action,
            onValueChanged = onValueChanged
        )

        is AuthAction.ProvideNumeric -> ProvideNumeric(
            context = context,
            authValue = authValue,
            action = action,
            onValueChanged = onValueChanged
        )

        is AuthAction.ProvideStaticKey -> ProvideStaticKey(
            context = context,
            authValue = authValue,
            onValueChanged = onValueChanged
        )
    }
}

@Composable
private fun DisplayAlphaNumeric(
    action: AuthAction.DisplayAlphaNumeric
) {
    val message = stringResource(id = R.string.label_display_alpha_numeric_rationale, action.text)
    val start = message.indexOf(action.text)
    val end = start + action.text.length
    val input = AnnotatedString.Builder(message)
        .apply {
            addStyle(
                style = SpanStyle(fontWeight = FontWeight.Bold),
                start = start,
                end = end
            )
        }.toAnnotatedString()
    Text(text = input, style = MaterialTheme.typography.bodyMedium)
    Spacer(modifier = Modifier.size(size = 16.dp))
}

@Composable
private fun DisplayNumber(
    action: AuthAction.DisplayNumber
) {
    val number = action.number.toString()
    val message = stringResource(id = R.string.label_display_alpha_numeric_rationale, number)
    val start = message.indexOf(number)
    val end = start + number.length
    val input = AnnotatedString.Builder(message)
        .apply {
            addStyle(
                style = SpanStyle(fontWeight = FontWeight.Bold),
                start = start,
                end = end
            )
        }.toAnnotatedString()
    Text(text = input, style = MaterialTheme.typography.bodyMedium)
    Spacer(modifier = Modifier.size(size = 16.dp))
}

@Composable
private fun ProvideAlphaNumeric(
    context: Context,
    action: AuthAction.ProvideAlphaNumeric,
    authValue: TextFieldValue,
    onValueChanged: (TextFieldValue) -> Unit
) {
    Text(
        text = stringResource(id = R.string.label_provide_alpha_numeric_rationale),
        style = MaterialTheme.typography.bodyMedium
    )
    Spacer(modifier = Modifier.size(size = 16.dp))
    MeshOutlinedTextField(
        modifier = Modifier.padding(vertical = 8.dp),
        externalLeadingIcon = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.Password,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        value = authValue,
        onValueChanged = {
            if (it.text.length <= action.maxNumberOfCharacters.toInt())
                onValueChanged(it)
        },
        label = { TextFieldValue(text = context.getString(R.string.label_auth_value)) },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Characters,
            autoCorrect = false
        ),
        placeholder = { TextFieldValue(text = "e.g. 12E4S6") },
        supportingText = {
            Text(
                text = "${authValue.text.length} / ${action.maxNumberOfCharacters}",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
            )
        }
    )
}

@Composable
private fun ProvideNumeric(
    context: Context,
    action: AuthAction.ProvideNumeric,
    authValue: TextFieldValue,
    onValueChanged: (TextFieldValue) -> Unit
) {
    Text(
        text = stringResource(id = R.string.label_provide_numeric_rationale),
        style = MaterialTheme.typography.bodyMedium
    )
    Spacer(modifier = Modifier.size(size = 16.dp))
    MeshOutlinedTextField(
        modifier = Modifier.padding(vertical = 8.dp),
        externalLeadingIcon = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.Password,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        value = authValue,
        onValueChanged = {
            if (it.text.length <= action.maxNumberOfDigits.toInt())
                onValueChanged(it)
        },
        label = { Text(text = context.getString(R.string.label_auth_value)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, autoCorrect = false),
        placeholder = { Text(text = "e.g. 123456") },
        supportingText = {
            Text(
                text = "${authValue.text.length} / ${action.maxNumberOfDigits}",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
            )
        }
    )
}

@Composable
private fun ProvideStaticKey(
    context: Context,
    authValue: TextFieldValue,
    onValueChanged: (TextFieldValue) -> Unit
) {
    Text(
        text = stringResource(id = R.string.label_provide_static_key_rationale),
        style = MaterialTheme.typography.bodyMedium
    )
    Spacer(modifier = Modifier.size(size = 16.dp))
    MeshOutlinedTextField(
        modifier = Modifier.padding(vertical = 8.dp),
        externalLeadingIcon = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.Password,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        value = authValue,
        onValueChanged = { if (it.text.length <= 32) onValueChanged(it) },
        label = { TextFieldValue(text = context.getString(R.string.label_auth_value)) },
        regex = Regex("[0-9A-Fa-f]{0,32}"),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Characters,
            autoCorrect = false
        ),
        placeholder = { TextFieldValue(text = "") }
    )
}