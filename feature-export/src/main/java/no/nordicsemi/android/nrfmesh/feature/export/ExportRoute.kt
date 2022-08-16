@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.feature.export

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.nrfmesh.core.ui.MeshLargeTopAppBar
import no.nordicsemi.android.nrfmesh.core.ui.RowItem
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.core.ui.showSnackbar
import no.nordicsemi.kotlin.mesh.core.exception.AtLeastOneNetworkKeyMustBeSelected
import no.nordicsemi.kotlin.mesh.core.exception.AtLeastOneProvisionerMustBeSelected
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import no.nordicsemi.kotlin.mesh.core.model.toHex
import no.nordicsemi.kotlin.mesh.crypto.Utils.encodeHex


@Composable
fun ExportRoute(
    viewModel: ExportViewModel = hiltViewModel(),
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    ExportScreen(
        context = context,
        uiState = viewModel.uiState,
        onExportEverythingToggled = { viewModel.onExportEverythingToggled(it) },
        onNetworkKeySelected = { key, selected -> viewModel.onNetworkKeySelected(key, selected) },
        onProvisionerSelected = { provisioner, selected ->
            viewModel.onProvisionerSelected(provisioner, selected)
        },
        onExportDeviceKeysToggled = { viewModel.onExportDeviceKeysToggled(it) },
        onExportClicked = { uri -> viewModel.export(context.contentResolver, uri) },
        onExportStateDisplayed = { viewModel.onExportStateDisplayed() },
        onBackPressed = onBackPressed
    )
}

@Composable
private fun ExportScreen(
    context: Context,
    uiState: ExportScreenUiState,
    onExportEverythingToggled: (Boolean) -> Unit,
    onNetworkKeySelected: (NetworkKey, Boolean) -> Unit,
    onProvisionerSelected: (Provisioner, Boolean) -> Unit,
    onExportDeviceKeysToggled: (Boolean) -> Unit,
    onExportClicked: (Uri) -> Unit,
    onExportStateDisplayed: () -> Unit,
    onBackPressed: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val createDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(stringResource(R.string.document_type)),
        onResult = { it?.let { onExportClicked(it) } }
    )
    when (uiState.exportState) {
        is ExportState.Success ->
            LaunchedEffect(key1 = snackbarHostState) {
                showSnackbar(
                    snackbarHostState = snackbarHostState,
                    message = context.getString(R.string.label_success)
                )
                onExportStateDisplayed()
            }
        is ExportState.Error -> {
            LaunchedEffect(key1 = snackbarHostState) {
                onExportStateDisplayed()
                showSnackbar(
                    snackbarHostState = snackbarHostState,
                    message = when (uiState.exportState.throwable) {
                        is AtLeastOneProvisionerMustBeSelected ->
                            context.getString(R.string.error_select_one_provisioner)
                        is AtLeastOneNetworkKeyMustBeSelected ->
                            context.getString(R.string.error_select_one_network_key)
                        else -> context.getString(R.string.error_unknown)
                    }
                )
            }
        }
        is ExportState.Unknown -> { /*Do nothing*/
        }
    }
    Scaffold(
        topBar = {
            MeshLargeTopAppBar(
                title = stringResource(id = R.string.label_export),
                actions = {
                    IconButton(
                        onClick = { createDocument.launch(uiState.networkName) },
                        enabled = uiState.enableExportButton
                    ) {
                        Icon(imageVector = Icons.Filled.Save, contentDescription = null)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onBackPressed() }) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            contentPadding = padding
        ) {
            item {
                ExportSelection(
                    uiState = uiState,
                    onExportEverythingToggled = onExportEverythingToggled
                )
            }
            if (uiState.exportEverything) {
                item {
                    Icon(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null
                    )
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = stringResource(id = R.string.label_export_configuration_rationale)
                    )
                }
            } else {
                uiState.apply {
                    item {
                        SectionTitle(title = stringResource(R.string.label_provisioners))
                    }
                    items(
                        items = provisionerItemStates,
                        key = { it.provisioner.uuid }
                    ) { state ->
                        ProvisionerRow(
                            state = state,
                            onProvisionerSelected = onProvisionerSelected,
                            exportEverything = uiState.exportEverything
                        )
                    }
                    item {
                        SectionTitle(title = stringResource(R.string.label_network_keys))
                    }
                    items(
                        items = networkKeyItemStates,
                        key = { it.networkKey.key }
                    ) { state ->
                        NetworkKeyRow(
                            state = state,
                            onNetworkKeySelected = onNetworkKeySelected,
                            exportEverything = uiState.exportEverything
                        )
                    }
                }
                item {
                    ExportDeviceKeys(
                        uiState = uiState,
                        onExportDeviceKeysToggled = onExportDeviceKeysToggled
                    )
                }
            }
        }
    }
}

@Composable
private fun ExportSelection(
    uiState: ExportScreenUiState,
    onExportEverythingToggled: (Boolean) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        OutlinedIconToggleButton(
            modifier = Modifier.weight(1f),
            checked = uiState.exportEverything,
            onCheckedChange = { onExportEverythingToggled(true) }
        ) {
            Text(text = "All")
        }
        Spacer(modifier = Modifier.size(16.dp))
        OutlinedIconToggleButton(
            modifier = Modifier.weight(1f),
            checked = !uiState.exportEverything,
            onCheckedChange = { onExportEverythingToggled(false) }
        ) {
            Text(text = "Partial")
        }
    }
    if (showDialog)
        AlertDialog(
            onDismissRequest = { showDialog = !showDialog },
            title = { Text(text = stringResource(id = R.string.label_export_configuration)) },
            text = {
                Text(text = stringResource(id = R.string.label_export_configuration_rationale))
            },
            confirmButton = {
                Button(onClick = { showDialog = !showDialog }) {
                    Text(text = "Ok")
                }
            },
            dismissButton = {}
        )
}

@Composable
private fun ProvisionerRow(
    state: ProvisionerItemState,
    onProvisionerSelected: (Provisioner, Boolean) -> Unit,
    exportEverything: Boolean
) {
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .fillMaxWidth()
            .padding(end = 16.dp)
            .clickable {
                onProvisionerSelected(state.provisioner, !state.isSelected)
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RowItem(
            modifier = Modifier.weight(1f),
            imageVector = Icons.Outlined.Groups,
            title = state.provisioner.name,
            subtitle = state.provisioner.node?.primaryUnicastAddress?.address?.toHex(
                prefix0x = true
            ) ?: ""
        )
        Divider(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 32.dp)
                .width(1.dp)
        )
        Checkbox(
            checked = state.isSelected,
            onCheckedChange = { onProvisionerSelected(state.provisioner, it) },
            enabled = !exportEverything
        )
    }
}

@Composable
private fun NetworkKeyRow(
    state: NetworkKeyItemState,
    onNetworkKeySelected: (NetworkKey, Boolean) -> Unit,
    exportEverything: Boolean
) {
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .fillMaxWidth()
            .padding(end = 16.dp)
            .clickable {
                onNetworkKeySelected(state.networkKey, !state.isSelected)
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RowItem(
            modifier = Modifier.weight(1f),
            imageVector = Icons.Outlined.VpnKey,
            title = state.networkKey.name,
            subtitle = state.networkKey.key.encodeHex(prefixOx = true),
            subtitleTextOverflow = TextOverflow.Ellipsis
        )
        Divider(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 32.dp)
                .width(1.dp)
        )
        Checkbox(
            checked = state.isSelected,
            onCheckedChange = { onNetworkKeySelected(state.networkKey, it) },
            enabled = !exportEverything
        )
    }
}

@Composable
private fun ExportDeviceKeys(
    uiState: ExportScreenUiState,
    onExportDeviceKeysToggled: (Boolean) -> Unit
) {
    SectionTitle(title = stringResource(R.string.label_device_keys))
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .fillMaxWidth()
            .padding(end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RowItem(
            modifier = Modifier.weight(1f),
            imageVector = Icons.Outlined.VpnKey,
            title = stringResource(R.string.label_export_device_keys),
            subtitle = stringResource(R.string.label_export_device_keys_rationale),
            subtitleMaxLines = Int.MAX_VALUE
        )
        Divider(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 32.dp)
                .width(1.dp)
        )
        Switch(
            checked = uiState.exportDeviceKeys,
            onCheckedChange = { onExportDeviceKeysToggled(it) },
            enabled = !uiState.exportEverything
        )
    }
}

@Composable
private fun parseExceptionState(exportState: ExportState) = stringResource(
    id = when (exportState) {
        is ExportState.Success -> R.string.label_success
        is ExportState.Error -> when (exportState.throwable) {
            is AtLeastOneProvisionerMustBeSelected -> R.string.error_select_one_provisioner
            is AtLeastOneNetworkKeyMustBeSelected -> R.string.error_select_one_network_key
            else -> R.string.error_unknown
        }
        else -> R.string.error_unknown
    }
)