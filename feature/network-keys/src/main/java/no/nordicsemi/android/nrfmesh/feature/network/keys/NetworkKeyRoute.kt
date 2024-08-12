package no.nordicsemi.android.nrfmesh.feature.network.keys

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AssistWalker
import androidx.compose.material.icons.outlined.AutoMode
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.LocalPolice
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItemTextField
import no.nordicsemi.android.nrfmesh.core.ui.MeshOutlinedTextField
import no.nordicsemi.android.nrfmesh.core.ui.MeshTwoLineListItem
import no.nordicsemi.android.nrfmesh.core.ui.showSnackbar
import no.nordicsemi.kotlin.data.toByteArray
import no.nordicsemi.kotlin.data.toHexString
import no.nordicsemi.kotlin.mesh.core.exception.InvalidKeyLength
import no.nordicsemi.kotlin.mesh.core.exception.KeyInUse
import no.nordicsemi.kotlin.mesh.core.model.Insecure
import no.nordicsemi.kotlin.mesh.core.model.KeyDistribution
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.KeyRefreshPhase
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.NormalOperation
import no.nordicsemi.kotlin.mesh.core.model.Secure
import no.nordicsemi.kotlin.mesh.core.model.Security
import no.nordicsemi.kotlin.mesh.core.model.UsingNewKeys
import java.text.DateFormat
import java.util.Date

@Composable
internal fun NetworkKeyRoute(
    uiState: NetworkKeyScreenUiState,
    onNameChanged: (String) -> Unit,
    onKeyChanged: (ByteArray) -> Unit
) {
    NetworkKeyScreen(
        keyState = uiState.keyState,
        onNameChanged = onNameChanged,
        onKeyChanged = onKeyChanged
    )
}

@Composable
private fun NetworkKeyScreen(
    keyState: KeyState,
    onNameChanged: (String) -> Unit,
    onKeyChanged: (ByteArray) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var isCurrentlyEditable by rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (keyState) {
            KeyState.Loading -> { /* Do nothing */
            }

            is KeyState.Success -> NetworkKeyInfo(
                snackbarHostState = snackbarHostState,
                networkKey = keyState.key,
                isCurrentlyEditable = isCurrentlyEditable,
                onEditableStateChanged = { isCurrentlyEditable = !isCurrentlyEditable },
                onNameChanged = onNameChanged,
                onKeyChanged = onKeyChanged
            )

            is KeyState.Error -> when (keyState.throwable) {
                is KeyInUse -> {}
                is InvalidKeyLength -> {}
            }
        }
    }
}

@Composable
private fun NetworkKeyInfo(
    snackbarHostState: SnackbarHostState,
    networkKey: NetworkKey,
    isCurrentlyEditable: Boolean,
    onEditableStateChanged: () -> Unit,
    onNameChanged: (String) -> Unit,
    onKeyChanged: (ByteArray) -> Unit
) {
    Name(
        name = networkKey.name,
        onNameChanged = onNameChanged,
        isCurrentlyEditable = isCurrentlyEditable,
        onEditableStateChanged = onEditableStateChanged
    )
    Key(
        snackbarHostState = snackbarHostState,
        networkKey = networkKey.key,
        isInUse = networkKey.isInUse,
        onKeyChanged = onKeyChanged,
        isCurrentlyEditable = isCurrentlyEditable,
        onEditableStateChanged = onEditableStateChanged
    )
    OldKey(oldKey = networkKey.oldKey)
    KeyIndex(index = networkKey.index)
    KeyRefreshPhase(phase = networkKey.phase)
    Security(security = networkKey.security)
    LastModified(networkKey.timestamp)
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
        onValueChanged = onNameChanged,
        isEditable = isCurrentlyEditable,
        onEditableStateChanged = onEditableStateChanged,
    )
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun Key(
    snackbarHostState: SnackbarHostState,
    networkKey: ByteArray,
    isInUse: Boolean,
    onKeyChanged: (ByteArray) -> Unit,
    isCurrentlyEditable: Boolean,
    onEditableStateChanged: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var key by rememberSaveable { mutableStateOf(networkKey.toHexString()) }
    var onEditClick by rememberSaveable { mutableStateOf(false) }

    ElevatedCard(modifier = Modifier.padding(horizontal = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                modifier = Modifier.padding(start = 12.dp),
                imageVector = Icons.Outlined.VpnKey,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
            Crossfade(targetState = onEditClick, label = "name") { state ->
                when (state) {
                    true ->
                        MeshOutlinedTextField(
                            onFocus = onEditClick,
                            value = key,
                            onValueChanged = { key = it },
                            label = { Text(text = stringResource(id = R.string.label_key)) },
                            placeholder = {
                                Text(
                                    text = stringResource(id = R.string.label_placeholder_key),
                                    maxLines = 1
                                )
                            },
                            internalTrailingIcon = {
                                IconButton(
                                    enabled = key.isNotBlank(),
                                    onClick = { key = "" }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Clear,
                                        contentDescription = null
                                    )
                                }
                            },
                            regex = Regex("[0-9A-Fa-f]{0,32}"),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                autoCorrectEnabled = false
                            ),
                            content = {
                                IconButton(
                                    modifier = Modifier.padding(start = 8.dp, end = 16.dp),
                                    enabled = key.length == 32,
                                    onClick = {
                                        onEditClick = !onEditClick
                                        onKeyChanged(key.toByteArray())
                                        onEditableStateChanged()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null
                                    )
                                }
                            }
                        )

                    false -> MeshTwoLineListItem(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        title = stringResource(id = R.string.label_key),
                        subtitle = key,
                        trailingComposable = {
                            IconButton(
                                enabled = isCurrentlyEditable,
                                onClick = {
                                    if (!isInUse) {
                                        onEditClick = !onEditClick
                                        onEditableStateChanged()
                                    } else {
                                        showSnackbar(
                                            scope = coroutineScope,
                                            snackbarHostState = snackbarHostState,
                                            message = context.getString(R.string.error_cannot_edit_key_in_use)
                                        )
                                    }
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

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun OldKey(oldKey: ByteArray?) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.AssistWalker,
        title = stringResource(id = R.string.label_old_key),
        subtitle = oldKey?.toHexString()
            ?: stringResource(id = R.string.label_na)
    )
}

@Composable
fun KeyIndex(index: KeyIndex) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.FormatListNumbered,
        title = stringResource(id = R.string.label_key_index),
        subtitle = index.toString()
    )
}

@Composable
fun KeyRefreshPhase(phase: KeyRefreshPhase) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.AutoMode,
        title = stringResource(id = R.string.label_key_refresh_phase),
        subtitle = phase.description()
    )
}

@Composable
fun Security(security: Security) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.LocalPolice,
        title = stringResource(id = R.string.label_security),
        subtitle = security.description()
    )
}

@Composable
fun LastModified(timestamp: Instant) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.Update,
        title = stringResource(id = R.string.label_last_modified),
        subtitle = DateFormat
            .getDateTimeInstance()
            .format(Date(timestamp.toEpochMilliseconds()))
    )
}

@Suppress("unused")
@Composable
fun KeyRefreshPhase.description(): String = when (this) {
    NormalOperation -> stringResource(id = R.string.label_normal_operation)
    KeyDistribution -> stringResource(id = R.string.label_key_distribution)
    UsingNewKeys -> stringResource(id = R.string.label_using_new_keys)
}

@Composable
fun Security.description(): String = when (this) {
    Secure -> stringResource(id = R.string.label_secure)
    Insecure -> stringResource(id = R.string.label_insecure)
}