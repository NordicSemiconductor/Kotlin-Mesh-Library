@file:OptIn(ExperimentalLifecycleComposeApi::class, ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.feature.provisioners

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.feature.provisioners.R
import no.nordicsemi.android.nrfmesh.core.ui.MeshLargeTopAppBar
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.android.nrfmesh.core.ui.MeshOutlinedTextField
import no.nordicsemi.android.nrfmesh.core.ui.MeshTwoLineListItem
import no.nordicsemi.kotlin.mesh.core.model.Address
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import no.nordicsemi.kotlin.mesh.crypto.Utils.encodeHex

@Composable
internal fun ProvisionerRoute(
    viewModel: ProvisionerViewModel = hiltViewModel(),
    onBackPressed: () -> Unit
) {
    val uiState: ProvisionerScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProvisionerScreen(
        provisionerState = uiState.provisionerState,
        onNameChanged = viewModel::onNameChanged,
        onAddressChanged = viewModel::onAddressChanged,
        onBackPressed = {
            viewModel.save()
            onBackPressed()
        }
    )
}

@Composable
private fun ProvisionerScreen(
    provisionerState: ProvisionerState,
    onNameChanged: (String) -> Unit,
    onAddressChanged: (String) -> Unit,
    onBackPressed: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(connection = scrollBehavior.nestedScrollConnection),
        topBar = {
            MeshLargeTopAppBar(
                title = stringResource(id = R.string.label_edit_provisioner),
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
        when (provisionerState) {
            ProvisionerState.Loading -> { /* Do nothing */
            }
            is ProvisionerState.Success -> {
                ProvisionerInfo(
                    padding = padding,
                    provisioner = provisionerState.provisioner,
                    onNameChanged = onNameChanged,
                    onAddressChanged = onAddressChanged
                )
            }
            is ProvisionerState.Error -> {
                MeshNoItemsAvailable(
                    imageVector = Icons.Outlined.Group,
                    title = provisionerState.throwable.message ?: "Unknown error"
                )
            }
        }
    }
}

@Composable
private fun ProvisionerInfo(
    padding: PaddingValues,
    provisioner: Provisioner,
    onNameChanged: (String) -> Unit,
    onAddressChanged: (String) -> Unit
) {
    var isCurrentlyEditable by rememberSaveable { mutableStateOf(true) }
    LazyColumn(
        contentPadding = padding,
        modifier = Modifier
            .fillMaxSize()
    ) {
        provisioner.run {
            item {
                Name(
                    name = name,
                    onNameChanged = onNameChanged,
                    isCurrentlyEditable = isCurrentlyEditable,
                    onEditableStateChanged = { isCurrentlyEditable = !isCurrentlyEditable })
            }
            item {
                UnicastAddress(
                    address = node?.primaryUnicastAddress?.address,
                    onAddressChanged = onAddressChanged,
                    isCurrentlyEditable = isCurrentlyEditable,
                    onEditableStateChanged = { isCurrentlyEditable = !isCurrentlyEditable }
                )
            }
            item { ttl(ttl = node?.defaultTTL) }
            item { deviceKey(key = provisioner.node?.deviceKey) }
        }
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
                placeholder = { Text(text = stringResource(id = R.string.label_placeholder_provisioner_name)) },
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
fun UnicastAddress(
    address: Address?,
    onAddressChanged: (String) -> Unit,
    isCurrentlyEditable: Boolean,
    onEditableStateChanged: () -> Unit,
) {
    var value by rememberSaveable { mutableStateOf(address?.toString() ?: "") }
    var onEditClick by rememberSaveable { mutableStateOf(false) }
    Crossfade(targetState = onEditClick) { state ->
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
                label = { Text(text = stringResource(id = R.string.label_unicast_address)) },
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
                            onAddressChanged(value)
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
                        imageVector = Icons.Outlined.Lan,
                        contentDescription = null,
                        tint = LocalContentColor.current.copy(alpha = 0.6f)
                    )
                },
                title = stringResource(id = R.string.label_unicast_address),
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
fun ttl(ttl: Int?) {
    MeshTwoLineListItem(
        leadingIcon = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.HourglassTop,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        title = stringResource(id = R.string.label_ttl),
        subtitle = ttl?.toString() ?: stringResource(R.string.label_not_applicable)
    )
}

@Composable
fun deviceKey(key: ByteArray?) {
    MeshTwoLineListItem(
        leadingIcon = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.VpnKey,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        title = stringResource(id = R.string.label_device_key),
        subtitle = key?.encodeHex() ?: stringResource(R.string.label_not_applicable)
    )
}