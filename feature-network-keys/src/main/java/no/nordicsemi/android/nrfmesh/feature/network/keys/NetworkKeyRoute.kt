package no.nordicsemi.android.nrfmesh.feature.network.keys

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.Instant
import no.nordicsemi.android.nrfmesh.core.ui.MeshOutlinedTextField
import no.nordicsemi.android.nrfmesh.core.ui.MeshTwoLineListItem
import no.nordicsemi.android.nrfmesh.core.ui.showSnackbar
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
import no.nordicsemi.kotlin.mesh.crypto.Utils.decodeHex
import no.nordicsemi.kotlin.mesh.crypto.Utils.encodeHex
import java.text.DateFormat
import java.util.Date

@Composable
internal fun NetworkKeyRoute(
    viewModel: NetworkKeyViewModel = hiltViewModel()
) {
    val uiState: NetworkKeyScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    NetworkKeyScreen(
        keyState = uiState.keyState,
        onNameChanged = viewModel::onNameChanged,
        onKeyChanged = viewModel::onKeyChanged
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

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        when (keyState) {
            KeyState.Loading -> { /* Do nothing */
            }
            is KeyState.Success -> networkKeyInfo(
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

private fun LazyListScope.networkKeyInfo(
    snackbarHostState: SnackbarHostState,
    networkKey: NetworkKey,
    isCurrentlyEditable: Boolean,
    onEditableStateChanged: () -> Unit,
    onNameChanged: (String) -> Unit,
    onKeyChanged: (ByteArray) -> Unit
) {
    item {
        Name(
            name = networkKey.name,
            onNameChanged = onNameChanged,
            isCurrentlyEditable = isCurrentlyEditable,
            onEditableStateChanged = onEditableStateChanged
        )
    }
    item {
        Key(
            snackbarHostState = snackbarHostState,
            networkKey = networkKey.key,
            isInUse = networkKey.isInUse,
            onKeyChanged = onKeyChanged,
            isCurrentlyEditable = isCurrentlyEditable,
            onEditableStateChanged = onEditableStateChanged
        )
    }
    item { OldKey(oldKey = networkKey.oldKey) }
    item { KeyIndex(index = networkKey.index) }
    item { KeyRefreshPhase(phase = networkKey.phase) }
    item { Security(security = networkKey.security) }
    item { LastModified(networkKey.timestamp) }
}

@Composable
fun Name(
    name: String,
    onNameChanged: (String) -> Unit,
    isCurrentlyEditable: Boolean,
    onEditableStateChanged: () -> Unit,
) {
    var value by rememberSaveable { mutableStateOf(name) }
    var onEditClick by rememberSaveable { mutableStateOf(false) }
    Crossfade(targetState = onEditClick, label = "name") { state ->
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
                label = { Text(text = stringResource(id = R.string.label_name)) },
                placeholder = { Text(text = stringResource(id = R.string.label_placeholder_name)) },
                internalTrailingIcon = {
                    IconButton(enabled = value.isNotBlank(), onClick = { value = "" }) {
                        Icon(imageVector = Icons.Outlined.Clear, contentDescription = null)
                    }
                },
                content = {
                    IconButton(
                        modifier = Modifier.padding(start = 8.dp, end = 16.dp),
                        enabled = value.isNotBlank(),
                        onClick = {
                            onEditClick = !onEditClick
                            onEditableStateChanged()
                            value = value.trim()
                            onNameChanged(value)
                        }
                    ) {
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
                title = stringResource(id = R.string.label_name),
                subtitle = value,
                trailingComposable = {
                    IconButton(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        enabled = isCurrentlyEditable,
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
    var key by rememberSaveable { mutableStateOf(networkKey.encodeHex()) }
    var onEditClick by rememberSaveable { mutableStateOf(false) }
    Crossfade(targetState = onEditClick, label = "name") { state ->
        when (state) {
            true ->
                MeshOutlinedTextField(
                    modifier = Modifier.padding(vertical = 8.dp),
                    onFocus = onEditClick,
                    externalLeadingIcon = {
                        Icon(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            imageVector = Icons.Outlined.VpnKey,
                            contentDescription = null,
                            tint = LocalContentColor.current.copy(alpha = 0.6f)
                        )
                    },
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
                        ) { Icon(imageVector = Icons.Outlined.Clear, contentDescription = null) }
                    },
                    regex = Regex("[0-9A-Fa-f]{0,32}"),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        autoCorrect = false
                    ),
                    content = {
                        IconButton(
                            modifier = Modifier.padding(start = 8.dp, end = 16.dp),
                            enabled = key.length == 32,
                            onClick = {
                                onEditClick = !onEditClick
                                onKeyChanged(key.decodeHex())
                                onEditableStateChanged()
                            }
                        ) { Icon(imageVector = Icons.Outlined.Check, contentDescription = null) }
                    }
                )
            false -> MeshTwoLineListItem(
                leadingComposable = {
                    Icon(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        imageVector = Icons.Outlined.VpnKey,
                        contentDescription = null,
                        tint = LocalContentColor.current.copy(alpha = 0.6f)
                    )
                },
                title = stringResource(id = R.string.label_key),
                subtitle = key,
                trailingComposable = {
                    IconButton(
                        modifier = Modifier.padding(horizontal = 16.dp),
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

@Composable
fun OldKey(oldKey: ByteArray?) {
    MeshTwoLineListItem(
        leadingComposable = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.AssistWalker,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        title = stringResource(id = R.string.label_old_key),
        subtitle = oldKey?.encodeHex()
            ?: stringResource(id = R.string.label_na)
    )
}

@Composable
fun KeyIndex(index: KeyIndex) {
    MeshTwoLineListItem(
        leadingComposable = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.FormatListNumbered,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        title = stringResource(id = R.string.label_key_index),
        subtitle = index.toString()
    )
}

@Composable
fun KeyRefreshPhase(phase: KeyRefreshPhase) {
    MeshTwoLineListItem(
        leadingComposable = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.AutoMode,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        title = stringResource(id = R.string.label_key_refresh_phase),
        subtitle = phase.description()
    )
}

@Composable
fun Security(security: Security) {
    MeshTwoLineListItem(
        leadingComposable = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.LocalPolice,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        title = stringResource(id = R.string.label_security),
        subtitle = security.description()
    )
}

@Composable
fun LastModified(timestamp: Instant) {
    MeshTwoLineListItem(
        leadingComposable = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.Update,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        title = stringResource(id = R.string.label_last_modified),
        subtitle = DateFormat
            .getDateTimeInstance()
            .format(
                Date(timestamp.toEpochMilliseconds())
            )
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