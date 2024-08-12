@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.feature.export

import android.content.ContentResolver
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedIconToggleButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.nrfmesh.core.ui.MeshTwoLineListItem
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.core.ui.showSnackbar
import no.nordicsemi.kotlin.data.toHexString
import no.nordicsemi.kotlin.mesh.core.exception.AtLeastOneNetworkKeyMustBeSelected
import no.nordicsemi.kotlin.mesh.core.exception.AtLeastOneProvisionerMustBeSelected
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.Provisioner


@Composable
fun ExportRoute(
    uiState: ExportScreenUiState,
    onExportEverythingToggled: (Boolean) -> Unit,
    onNetworkKeySelected: (NetworkKey, Boolean) -> Unit,
    onProvisionerSelected: (Provisioner, Boolean) -> Unit,
    onExportDeviceKeysToggled: (Boolean) -> Unit,
    onExportClicked: (ContentResolver, Uri) -> Unit,
    onExportStateDisplayed: () -> Unit,
    onBackPressed: () -> Unit
) {
    ExportScreen(
        uiState = uiState,
        onExportEverythingToggled = onExportEverythingToggled,
        onNetworkKeySelected = onNetworkKeySelected,
        onProvisionerSelected = onProvisionerSelected,
        onExportDeviceKeysToggled = onExportDeviceKeysToggled,
        onExportClicked = onExportClicked,
        onExportStateDisplayed = onExportStateDisplayed,
        onBackPressed = onBackPressed
    )
}

@Composable
private fun ExportScreen(
    uiState: ExportScreenUiState,
    onExportEverythingToggled: (Boolean) -> Unit,
    onNetworkKeySelected: (NetworkKey, Boolean) -> Unit,
    onProvisionerSelected: (Provisioner, Boolean) -> Unit,
    onExportDeviceKeysToggled: (Boolean) -> Unit,
    onExportClicked: (ContentResolver, Uri) -> Unit,
    onExportStateDisplayed: () -> Unit,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val createDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(stringResource(R.string.document_type)),
        onResult = { it?.let { onExportClicked(context.contentResolver, it) } }
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
    /*Scaffold(
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
    }*/
    LazyColumn(
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

@OptIn(ExperimentalStdlibApi::class)
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
        MeshTwoLineListItem(
            modifier = Modifier.weight(1f),
            imageVector = Icons.Outlined.Groups,
            title = state.provisioner.name,
            subtitle = state.provisioner.node?.primaryUnicastAddress?.address?.toString() ?: ""
        )
        HorizontalDivider(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 32.dp)
        )
        Checkbox(
            checked = state.isSelected,
            onCheckedChange = { onProvisionerSelected(state.provisioner, it) },
            enabled = !exportEverything
        )
    }
}

@OptIn(ExperimentalStdlibApi::class)
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
        MeshTwoLineListItem(
            modifier = Modifier.weight(1f),
            imageVector = Icons.Outlined.VpnKey,
            title = state.networkKey.name,
            subtitle = state.networkKey.key.toHexString(prefixOx = true),
            subtitleTextOverflow = TextOverflow.Ellipsis
        )
        HorizontalDivider(
            modifier = Modifier
                .padding(
                    horizontal = 16.dp,
                    vertical = 32.dp
                )
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
        MeshTwoLineListItem(
            modifier = Modifier.weight(1f),
            imageVector = Icons.Outlined.VpnKey,
            title = stringResource(R.string.label_export_device_keys),
            subtitle = stringResource(R.string.label_export_device_keys_rationale),
            subtitleMaxLines = Int.MAX_VALUE
        )
        HorizontalDivider()
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