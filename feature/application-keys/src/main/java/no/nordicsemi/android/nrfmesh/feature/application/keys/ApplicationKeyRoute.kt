@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.application.keys

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AssistWalker
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItemTextField
import no.nordicsemi.android.nrfmesh.core.ui.MeshOutlinedTextField
import no.nordicsemi.android.nrfmesh.core.ui.MeshTwoLineListItem
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.core.ui.showSnackbar
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeyScreen
import no.nordicsemi.kotlin.data.toByteArray
import no.nordicsemi.kotlin.data.toHexString
import no.nordicsemi.kotlin.mesh.core.exception.InvalidKeyLength
import no.nordicsemi.kotlin.mesh.core.exception.KeyInUse
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey

@Composable
internal fun ApplicationKeyRoute(
    appState: AppState,
    uiState: ApplicationKeyScreenUiState,
    onNameChanged: (String) -> Unit,
    onKeyChanged: (ByteArray) -> Unit,
    onBoundNetworkKeyChanged: (NetworkKey) -> Unit,
    onBackPressed: () -> Unit
) {
    val screen = appState.currentScreen as? ApplicationKeyScreen
    LaunchedEffect(key1 = screen) {
        screen?.buttons?.onEach { button ->
            when (button) {
                ApplicationKeyScreen.Actions.BACK -> onBackPressed()
            }
        }?.launchIn(this)
    }
    ApplicationKeyScreen(
        keyState = uiState.keyState,
        onNameChanged = onNameChanged,
        onKeyChanged = onKeyChanged,
        onBoundNetworkKeyChanged = onBoundNetworkKeyChanged,
    )
}

@Composable
private fun ApplicationKeyScreen(
    keyState: KeyState,
    onNameChanged: (String) -> Unit,
    onKeyChanged: (ByteArray) -> Unit,
    onBoundNetworkKeyChanged: (NetworkKey) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isCurrentlyEditable by rememberSaveable { mutableStateOf(true) }
    var boundNetKeyIndex by rememberSaveable { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        when (keyState) {
            KeyState.Loading -> { /* Do nothing */
            }

            is KeyState.Success -> {
                boundNetKeyIndex = keyState.key.boundNetKeyIndex.toInt()
                applicationKeyInfo(
                    snackbarHostState = snackbarHostState,
                    applicationKey = keyState.key,
                    isCurrentlyEditable = isCurrentlyEditable,
                    onEditableStateChanged = { isCurrentlyEditable = !isCurrentlyEditable },
                    onNameChanged = onNameChanged,
                    onKeyChanged = onKeyChanged
                )
                boundNetworkKeys(
                    context = context,
                    coroutineScope = coroutineScope,
                    snackbarHostState = snackbarHostState,
                    isInUse = keyState.key.isInUse,
                    boundNetKeyIndex = boundNetKeyIndex,
                    networkKeys = keyState.networkKeys,
                    onBoundNetworkKeyChanged = {
                        boundNetKeyIndex = it.index.toInt()
                        onBoundNetworkKeyChanged(it)
                    }
                )
            }

            is KeyState.Error -> when (keyState.throwable) {
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
            Crossfade(targetState = onEditClick, label = "key") { state ->
                when (state) {
                    true ->
                        MeshOutlinedTextField(
                            modifier = Modifier.padding(vertical = 8.dp),
                            onFocus = onEditClick,
                            leadingComposable = {
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
    }
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun OldKey(oldKey: ByteArray?) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.AssistWalker,
        title = stringResource(id = R.string.label_old_key),
        subtitle = oldKey?.toHexString() ?: stringResource(id = R.string.label_na)
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

@OptIn(ExperimentalStdlibApi::class)
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
        ElevatedCardItem(
            modifier = Modifier
                .padding(horizontal = 8.dp),
            onClick = {
                if (!isInUse) onBoundNetworkKeyChanged(key)
                else showSnackbar(
                    scope = coroutineScope,
                    snackbarHostState = snackbarHostState,
                    message = context.getString(R.string.error_cannot_change_bound_net_key),
                    withDismissAction = true
                )
            },
            imageVector = Icons.Outlined.VpnKey,
            title = key.name,
            titleAction = {
                if (key.index.toInt() == boundNetKeyIndex) {
                    Icon(
                        modifier = Modifier
                            .padding(start = 16.dp),
                        imageVector = Icons.Outlined.TaskAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.surfaceTint
                    )
                }
            },
            subtitle = key.key.toHexString(),
        )
    }
}