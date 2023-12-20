@file:OptIn(ExperimentalMaterial3Api::class)
@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.application.keys

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.android.feature.application.keys.R
import no.nordicsemi.android.nrfmesh.core.ui.*
import no.nordicsemi.kotlin.mesh.core.exception.InvalidKeyLength
import no.nordicsemi.kotlin.mesh.core.exception.KeyInUse
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.crypto.Utils.decodeHex
import no.nordicsemi.kotlin.mesh.crypto.Utils.encodeHex

@Composable
internal fun ApplicationKeyRoute(viewModel: ApplicationKeyViewModel = hiltViewModel()) {
    val uiState: ApplicationKeyScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    ApplicationKeyScreen(
        applicationKeyState = uiState.applicationKeyState,
        onNameChanged = viewModel::onNameChanged,
        onKeyChanged = viewModel::onKeyChanged,
        onBoundNetworkKeyChanged = viewModel::onBoundNetworkKeyChanged
    )
}

@Composable
private fun ApplicationKeyScreen(
    applicationKeyState: ApplicationKeyState,
    onNameChanged: (String) -> Unit,
    onKeyChanged: (ByteArray) -> Unit,
    onBoundNetworkKeyChanged: (NetworkKey) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isCurrentlyEditable by rememberSaveable { mutableStateOf(true) }
    var boundNetKeyIndex by rememberSaveable { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        when (applicationKeyState) {
            ApplicationKeyState.Loading -> { /* Do nothing */
            }
            is ApplicationKeyState.Success -> {
                boundNetKeyIndex = applicationKeyState.applicationKey.boundNetKeyIndex.toInt()
                applicationKeyInfo(
                    snackbarHostState = snackbarHostState,
                    applicationKey = applicationKeyState.applicationKey,
                    isCurrentlyEditable = isCurrentlyEditable,
                    onEditableStateChanged = { isCurrentlyEditable = !isCurrentlyEditable },
                    onNameChanged = onNameChanged,
                    onKeyChanged = onKeyChanged
                )
                boundNetworkKeys(
                    context = context,
                    coroutineScope = coroutineScope,
                    snackbarHostState = snackbarHostState,
                    isInUse = applicationKeyState.applicationKey.isInUse,
                    boundNetKeyIndex = boundNetKeyIndex,
                    networkKeys = applicationKeyState.networkKeys,
                    onBoundNetworkKeyChanged = {
                        boundNetKeyIndex = it.index.toInt()
                        onBoundNetworkKeyChanged(it)
                    }
                )
            }
            is ApplicationKeyState.Error -> when (applicationKeyState.throwable) {
                is KeyInUse -> {}
                is InvalidKeyLength -> {}
            }
        }
    }
}

private fun LazyListScope.applicationKeyInfo(
    snackbarHostState: SnackbarHostState,
    applicationKey: ApplicationKey,
    isCurrentlyEditable: Boolean,
    onEditableStateChanged: () -> Unit,
    onNameChanged: (String) -> Unit,
    onKeyChanged: (ByteArray) -> Unit
) {
    item {
        Name(
            name = applicationKey.name,
            onNameChanged = onNameChanged,
            isCurrentlyEditable = isCurrentlyEditable,
            onEditableStateChanged = onEditableStateChanged
        )
    }
    item {
        Key(
            snackbarHostState = snackbarHostState,
            networkKey = applicationKey.key,
            isInUse = applicationKey.isInUse,
            onKeyChanged = onKeyChanged,
            isCurrentlyEditable = isCurrentlyEditable,
            onEditableStateChanged = onEditableStateChanged
        )
    }
    item {
        OldKey(oldKey = applicationKey.oldKey)
    }
    item {
        KeyIndex(index = applicationKey.index)
    }
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
    Crossfade(targetState = onEditClick, label = "key") { state ->
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

private fun LazyListScope.boundNetworkKeys(
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    context: Context,
    isInUse: Boolean,
    boundNetKeyIndex: Int,
    networkKeys: List<NetworkKey>,
    onBoundNetworkKeyChanged: (NetworkKey) -> Unit
) {
    item {
        SectionTitle(title = context.getString(R.string.label_bound_network_key))
    }
    items(
        items = networkKeys
    ) { key ->
        MeshTwoLineListItem(
            modifier = Modifier.clickable {
                if (!isInUse) onBoundNetworkKeyChanged(key)
                else showSnackbar(
                    scope = coroutineScope,
                    snackbarHostState = snackbarHostState,
                    message = context.getString(R.string.error_cannot_change_bound_net_key),
                    withDismissAction = true
                )
            },
            leadingComposable = {
                Icon(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    imageVector = Icons.Outlined.VpnKey,
                    contentDescription = null,
                    tint = LocalContentColor.current.copy(alpha = 0.6f)
                )
            },
            title = key.name,
            subtitle = key.key.encodeHex(),
            trailingComposable = {
                if (key.index.toInt() == boundNetKeyIndex) {
                    Icon(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(end = 16.dp),
                        imageVector = Icons.Outlined.TaskAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.surfaceTint
                    )
                }
            }
        )
    }
}