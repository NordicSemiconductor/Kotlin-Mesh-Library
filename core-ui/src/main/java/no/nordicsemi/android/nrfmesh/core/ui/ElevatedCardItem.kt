package no.nordicsemi.android.nrfmesh.core.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ElevatedCardItem(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    title: String,
    titleAction: @Composable () -> Unit = {},
    subtitle: String = "",
    supportingText: String? = null,
    actions: @Composable (RowScope?.() -> Unit)? = null,
) {
    ElevatedCard(modifier = modifier) {
        MeshTwoLineListItem(
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

@Composable
fun ElevatedCardItemTextField(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    title: String,
    subtitle: String = "",
    placeholder: String = "",
    onValueChanged: (String) -> Unit
){
    var value by rememberSaveable { mutableStateOf(subtitle) }
    var onEditClick by rememberSaveable { mutableStateOf(false) }
    ElevatedCard(
        modifier = modifier
            .clickable { onEditClick = !onEditClick }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                modifier = Modifier.padding(start = 12.dp),
                imageVector = imageVector,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
            Crossfade(targetState = onEditClick, label = "name") { state ->
                when (state) {
                    true -> MeshOutlinedTextField(
                        onFocus = onEditClick,
                        value = value,
                        onValueChanged = { value = it },
                        label = { Text(text = title) },
                        placeholder = {
                            Text(text = placeholder)
                        },
                        internalTrailingIcon = {
                            IconButton(enabled = value.isNotBlank(), onClick = { value = "" }) {
                                Icon(imageVector = Icons.Outlined.Clear, contentDescription = null)
                            }
                        },
                        content = {
                            IconButton(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                enabled = value.isNotBlank(),
                                onClick = {
                                    onEditClick = !onEditClick
                                    value = value.trim()
                                    onValueChanged(value)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                )
                            }
                        }
                    )

                    false -> MeshTwoLineListItem(
                        title = title,
                        subtitle = value,
                        trailingComposable = {
                            IconButton(
                                onClick = {
                                    onEditClick = !onEditClick
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