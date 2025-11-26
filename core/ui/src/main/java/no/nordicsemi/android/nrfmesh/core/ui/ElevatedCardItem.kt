package no.nordicsemi.android.nrfmesh.core.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp


@Composable
fun ElevatedCardItem(
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.outlinedCardColors(),
    imageVector: ImageVector,
    title: String,
    titleAction: @Composable () -> Unit = {},
    subtitle: String? = null,
    supportingText: String? = null,
    body: @Composable (ColumnScope?.() -> Unit)? = null,
    actions: @Composable (RowScope?.() -> Unit)? = null,
) {
    OutlinedCard(modifier = modifier, colors = colors) {
        MeshTwoLineListItem(
            modifier = Modifier.padding(horizontal = 16.dp),
            leadingComposable = {
                Icon(
                    modifier = Modifier.padding(end = 16.dp),
                    imageVector = imageVector,
                    contentDescription = null,
                    tint = LocalContentColor.current.copy(alpha = 0.6f)
                )
            },
            title = title,
            subtitle = subtitle,
            trailingComposable = titleAction
        )
        if (supportingText != null)
            Text(
                modifier = Modifier.padding(start = 58.dp, end = 16.dp, bottom = 8.dp),
                text = supportingText,
                style = MaterialTheme.typography.bodyMedium
            )
        body?.let {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .padding(horizontal = 16.dp)
            ) {
                it()
            }
        }
        actions?.let {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                it()
            }
        }
    }
}

@Composable
fun ElevatedCardItem(
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.outlinedCardColors(),
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    imageVector: ImageVector,
    title: String,
    titleAction: @Composable () -> Unit = {},
    subtitle: String? = null,
    supportingText: String? = null,
    actions: @Composable (RowScope?.() -> Unit)? = null,
) {
    if (onClick == null) {
        ElevatedCardItem(
            modifier = modifier,
            colors = colors,
            imageVector = imageVector,
            title = title,
            titleAction = titleAction,
            subtitle = subtitle,
            supportingText = supportingText,
            actions = actions
        )
    } else {
        OutlinedCard(
            modifier = modifier,
            onClick = onClick,
            enabled = enabled,
            colors = colors
        ) {
            MeshTwoLineListItem(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                leadingComposable = {
                    Icon(
                        modifier = Modifier.padding(end = 16.dp),
                        imageVector = imageVector,
                        contentDescription = null,
                        tint = LocalContentColor.current.copy(alpha = 0.6f)
                    )
                },
                title = title,
                subtitle = subtitle,
                trailingComposable = titleAction
            )
            if (supportingText != null)
                Text(
                    modifier = Modifier.padding(start = 58.dp, end = 16.dp, bottom = 16.dp),
                    text = supportingText,
                    style = MaterialTheme.typography.bodyMedium
                )
            actions?.let {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    it()
                }
            }
        }
    }
}

@Composable
fun ElevatedCardItem(
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.outlinedCardColors(),
    onClick: () -> Unit,
    image: @Composable () -> Unit,
    title: String,
    titleAction: @Composable () -> Unit = {},
    subtitle: String? = null,
    supportingText: String? = null,
    actions: @Composable (RowScope?.() -> Unit)? = null,
) {
    OutlinedCard(
        modifier = modifier,
        onClick = onClick,
        colors = colors
    ) {
        MeshTwoLineListItem(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            leadingComposable = { image() },
            title = title,
            subtitle = subtitle,
            trailingComposable = titleAction
        )
        if (supportingText != null)
            Text(
                modifier = Modifier.padding(start = 58.dp, end = 16.dp, bottom = 16.dp),
                text = supportingText,
                style = MaterialTheme.typography.bodyMedium
            )
        actions?.let {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                it()
            }
        }
    }
}

@Composable
fun ElevatedCardItemTextField(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    title: String,
    subtitle: String = "",
    placeholder: String = "",
    onValueChanged: (String) -> Unit,
    isEditable: Boolean = true,
    onEditableStateChanged: () -> Unit = {},
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    regex: Regex? = null,
    isError: Boolean = regex != null && !regex.matches(subtitle),
    supportingText: @Composable (() -> Unit)? = null,
) {
    var value by rememberSaveable(inputs = arrayOf(subtitle), stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = subtitle, selection = TextRange(subtitle.length)))
    }
    var onEditClick by rememberSaveable { mutableStateOf(false) }
    OutlinedCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                modifier = Modifier.padding(start = 12.dp),
                imageVector = imageVector,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
            Crossfade(targetState = onEditClick, label = "textfield") { state ->
                when (state) {
                    true -> MeshOutlinedTextField(
                        modifier = Modifier.padding(start = 16.dp),
                        onFocus = onEditClick,
                        value = value,
                        onValueChanged = { value = it },
                        label = { Text(text = title) },
                        placeholder = { Text(text = placeholder, maxLines = 1) },
                        internalTrailingIcon = {
                            IconButton(
                                modifier = Modifier.padding(start = 8.dp),
                                enabled = value.text.isNotBlank(),
                                onClick = {
                                    value = TextFieldValue(
                                        text = "",
                                        selection = TextRange("".length)
                                    )
                                },
                                content = {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteSweep,
                                        contentDescription = null
                                    )
                                }
                            )
                        },
                        readOnly = readOnly,
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions,
                        regex = regex,
                        isError = isError,
                        supportingText = supportingText,
                        content = {
                            IconButton(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                onClick = {
                                    onEditClick = !onEditClick
                                    onEditableStateChanged()
                                    value = TextFieldValue(
                                        text = subtitle,
                                        selection = TextRange(subtitle.length)
                                    )
                                    onValueChanged(subtitle)
                                },
                                content = {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = null,
                                        tint = LocalContentColor.current.copy(alpha = 0.6f)
                                    )
                                }
                            )
                            IconButton(
                                modifier = Modifier.padding(end = 8.dp),
                                enabled = value.text.isNotBlank() && regex?.matches(value.text) ?: true,
                                onClick = {
                                    onEditClick = !onEditClick
                                    onEditableStateChanged()
                                    value = TextFieldValue(
                                        text = value.text.trim(),
                                        selection = TextRange(value.text.trim().length)
                                    )
                                    onValueChanged(value.text)
                                },
                                content = {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null,
                                        tint = LocalContentColor.current.copy(alpha = 0.6f)
                                    )
                                }
                            )
                        }
                    )

                    false -> MeshTwoLineListItem(
                        modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                        title = title,
                        subtitle = value.text,
                        trailingComposable = {
                            IconButton(
                                modifier = Modifier.padding(start = 8.dp),
                                enabled = isEditable,
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
    }
}

@Composable
fun ElevatedCardItemHexTextField(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    prefix: @Composable (() -> Unit)? = null,
    title: String,
    subtitle: String = "",
    placeholder: String = "",
    onValueChanged: (String) -> Unit,
    isEditable: Boolean = true,
    onEditableStateChanged: () -> Unit = {},
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    regex: Regex? = null,
    isError: Boolean = regex != null && !regex.matches(subtitle),
    supportingText: @Composable (() -> Unit)? = null,
) {
    var value by rememberSaveable(inputs = arrayOf(subtitle), stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = subtitle, selection = TextRange(subtitle.length)))
    }
    var onEditClick by rememberSaveable { mutableStateOf(false) }
    OutlinedCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                modifier = Modifier.padding(start = 12.dp),
                imageVector = imageVector,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
            Crossfade(targetState = onEditClick, label = "textfield") { state ->
                when (state) {
                    true -> MeshOutlinedTextField(
                        modifier = Modifier.padding(start = 16.dp),
                        prefix = prefix,
                        onFocus = onEditClick,
                        value = value,
                        onValueChanged = { value = it },
                        label = { Text(text = title) },
                        placeholder = { Text(text = placeholder, maxLines = 1) },
                        internalTrailingIcon = {
                            IconButton(
                                modifier = Modifier.padding(start = 8.dp),
                                enabled = value.text.isNotBlank(),
                                onClick = {
                                    value = TextFieldValue(
                                        text = "",
                                        selection = TextRange("".length)
                                    )
                                },
                                content = {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteSweep,
                                        contentDescription = null
                                    )
                                }
                            )
                        },
                        readOnly = readOnly,
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions,
                        regex = regex,
                        isError = isError,
                        supportingText = supportingText,
                        content = {
                            IconButton(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                onClick = {
                                    onEditClick = !onEditClick
                                    onEditableStateChanged()
                                    value = TextFieldValue(
                                        text = subtitle,
                                        selection = TextRange(subtitle.length)
                                    )
                                    onValueChanged(subtitle)
                                },
                                content = {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = null,
                                        tint = LocalContentColor.current.copy(alpha = 0.6f)
                                    )
                                }
                            )
                            IconButton(
                                modifier = Modifier.padding(end = 8.dp),
                                enabled = value.text.isNotBlank() && regex?.matches(value.text) ?: true,
                                onClick = {
                                    onEditClick = !onEditClick
                                    onEditableStateChanged()
                                    value = TextFieldValue(
                                        text = value.text.trim(),
                                        selection = TextRange(value.text.trim().length)
                                    )
                                    onValueChanged(value.text)
                                },
                                content = {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null,
                                        tint = LocalContentColor.current.copy(alpha = 0.6f)
                                    )
                                }
                            )
                        }
                    )

                    false -> MeshTwoLineListItem(
                        modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                        title = title,
                        subtitle = if (prefix != null) "0x${value.text}" else value.text,
                        trailingComposable = {
                            IconButton(
                                modifier = Modifier.padding(start = 8.dp),
                                enabled = isEditable,
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
    }
}