@file:OptIn(ExperimentalLifecycleComposeApi::class, ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.feature.network.keys

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import no.nordicsemi.android.nrfmesh.core.ui.MeshLargeTopAppBar
import no.nordicsemi.android.nrfmesh.core.ui.MeshOutlinedTextField
import no.nordicsemi.android.nrfmesh.core.ui.MeshTwoLineListItem
import no.nordicsemi.android.nrfmesh.core.ui.showSnackbar
import no.nordicsemi.kotlin.mesh.core.exception.InvalidKeyLength
import no.nordicsemi.kotlin.mesh.core.exception.KeyInUse
import no.nordicsemi.kotlin.mesh.core.model.*
import no.nordicsemi.kotlin.mesh.crypto.Utils.decodeHex
import no.nordicsemi.kotlin.mesh.crypto.Utils.encodeHex
import java.text.DateFormat
import java.util.*

@Composable
fun NetworkKeyRoute(
    viewModel: NetworkKeyViewModel = hiltViewModel(),
    onBackPressed: () -> Unit
) {
    val uiState: NetworkKeyScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    NetworkKeyScreen(
        networkKeyState = uiState.networkKeyState,
        onBackPressed = onBackPressed,
        onNameChanged = viewModel::onNameChanged,
        onKeyChanged = viewModel::onKeyChanged
    )
}

@Composable
private fun NetworkKeyScreen(
    networkKeyState: NetworkKeyState,
    onBackPressed: () -> Unit,
    onNameChanged: (String) -> Unit,
    onKeyChanged: (ByteArray) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var isCurrentlyEditable by rememberSaveable { mutableStateOf(true) }
    Scaffold(
        modifier = Modifier.nestedScroll(connection = scrollBehavior.nestedScrollConnection),
        topBar = {
            MeshLargeTopAppBar(
                title = stringResource(id = R.string.label_edit_network_key),
                navigationIcon = {
                    IconButton(onClick = { onBackPressed() }) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier
                .fillMaxSize()
        ) {

            when (networkKeyState) {
                NetworkKeyState.Loading -> { /* Do nothing */
                }
                is NetworkKeyState.Success -> {
                    networkKeyInfo(
                        snackbarHostState = snackbarHostState,
                        networkKey = networkKeyState.networkKey,
                        isCurrentlyEditable = isCurrentlyEditable,
                        onEditableStateChanged = { isCurrentlyEditable = !isCurrentlyEditable },
                        onNameChanged = onNameChanged,
                        onKeyChanged = onKeyChanged
                    )
                }
                is NetworkKeyState.Error -> {
                    when (networkKeyState.throwable) {
                        is KeyInUse -> {}
                        is InvalidKeyLength -> {}
                    }
                }
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
            isInUse = networkKey.isInUse(),
            onKeyChanged = onKeyChanged,
            isCurrentlyEditable = isCurrentlyEditable,
            onEditableStateChanged = onEditableStateChanged
        )
    }
    item {
        OldKey(oldKey = networkKey.oldKey)
    }
    item {
        KeyIndex(index = networkKey.index)
    }
    item {
        KeyRefreshPhase(phase = networkKey.phase)
    }
    item {
        Security(security = networkKey.security)
    }
    item {
        LastModified(networkKey.timestamp)
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
    Crossfade(targetState = onEditClick) { state ->
        when (state) {
            true -> MeshOutlinedTextField(
                modifier = Modifier.padding(vertical = 8.dp),
                onFocus =  onEditClick,
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
                leadingIcon = {
                    Icon(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        imageVector = Icons.Outlined.Badge,
                        contentDescription = null,
                        tint = LocalContentColor.current.copy(alpha = 0.6f)
                    )
                },
                title = stringResource(id = R.string.label_name),
                subtitle = value,
                trailingIcon = {
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
    Crossfade(targetState = onEditClick) { state ->
        when (state) {
            true ->
                MeshOutlinedTextField(
                    modifier = Modifier.padding(vertical = 8.dp),
                    onFocus =  onEditClick,
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
                leadingIcon = {
                    Icon(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        imageVector = Icons.Outlined.VpnKey,
                        contentDescription = null,
                        tint = LocalContentColor.current.copy(alpha = 0.6f)
                    )
                },
                title = stringResource(id = R.string.label_key),
                subtitle = key,
                trailingIcon = {
                    IconButton(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        enabled = isCurrentlyEditable,
                        onClick = {
                            if (!isInUse) {
                                onEditClick = !onEditClick
                                onEditableStateChanged()
                            } else {
                                coroutineScope.launch {
                                    showSnackbar(
                                        snackbarHostState = snackbarHostState,
                                        message = context.getString(R.string.error_cannot_edit_key_in_use)
                                    )
                                }
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
        leadingIcon = {
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
        leadingIcon = {
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
        leadingIcon = {
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
        leadingIcon = {
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
        leadingIcon = {
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

@Composable
fun RequestFocus(shouldFocus: Boolean, requester: FocusRequester) {
    SideEffect {
        if (shouldFocus) {
            requester.requestFocus()
        }
    }
}