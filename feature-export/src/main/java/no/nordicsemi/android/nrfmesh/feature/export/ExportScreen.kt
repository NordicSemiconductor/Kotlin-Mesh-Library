package no.nordicsemi.android.nrfmesh.feature.export

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.nrfmesh.core.ui.RowItem
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import no.nordicsemi.kotlin.mesh.core.model.toHex
import no.nordicsemi.kotlin.mesh.crypto.Utils.encodeHex

@Composable
fun ExportRoute(viewModel: ExportViewModel = hiltViewModel()) {
    ExportScreen(viewModel = viewModel)
}

@Composable
private fun ExportScreen(viewModel: ExportViewModel) {
    LazyColumn {
        item {
            ExportEverything(viewModel = viewModel)
        }
        if (!viewModel.exportUiState.exportEverything) {
            item {
                SectionTitle(title = stringResource(R.string.label_provisioners))
            }
            items(
                items = viewModel.exportUiState.provisionerItemStates,
                key = { it.provisioner.uuid }
            ) { state ->
                ProvisionerRow(
                    state = state,
                    onProvisionerSelected = { provisioner, isSelected ->
                        viewModel.onProvisionerSelected(
                            provisioner = provisioner,
                            selected = isSelected
                        )
                    }
                )
            }
            item {
                SectionTitle(title = stringResource(R.string.label_network_keys))
            }
            items(
                items = viewModel.exportUiState.networkKeyItemStates,
                key = { it.networkKey.key }
            ) { state ->
                NetworkKeyRow(
                    state = state,
                    onNetworkKeySelected = { key, isSelected ->
                        viewModel.onNetworkKeySelected(key = key, selected = isSelected)
                    }
                )
            }
            item {
                ExportDeviceKeys(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun ExportEverything(viewModel: ExportViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    SectionTitle(title = stringResource(R.string.label_export_configuration))
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .fillMaxWidth()
            .padding(end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RowItem(
            modifier = Modifier.weight(1f),
            imageVector = Icons.Outlined.FileDownload,
            title = stringResource(R.string.label_everything),
            subtitleMaxLines = Int.MAX_VALUE
        )
        Icon(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clickable {
                    showDialog = !showDialog
                },
            imageVector = Icons.Outlined.Info, contentDescription = null
        )
        Switch(
            checked = viewModel.exportUiState.exportEverything,
            onCheckedChange = {
                viewModel.onExportEverythingToggled(it)
            })
    }

    if (showDialog)
        AlertDialog(
            onDismissRequest = { showDialog = !showDialog },
            title = { Text(text = stringResource(id = R.string.label_export_configuration)) },
            text = { Text(text = stringResource(id = R.string.label_export_configuration_rationale)) },
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
    onProvisionerSelected: (Provisioner, Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .fillMaxWidth()
            .padding(end = 16.dp)
            .clickable { onProvisionerSelected(state.provisioner, !state.isSelected) },
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
            onCheckedChange = { onProvisionerSelected(state.provisioner, it) }
        )
    }
}

@Composable
private fun NetworkKeyRow(
    state: NetworkKeyItemState,
    onNetworkKeySelected: (NetworkKey, Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .fillMaxWidth()
            .padding(end = 16.dp)
            .clickable { onNetworkKeySelected(state.networkKey, !state.isSelected) },
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
            modifier = Modifier
                .clickable { onNetworkKeySelected(state.networkKey, !state.isSelected) },
            checked = state.isSelected,
            onCheckedChange = { onNetworkKeySelected(state.networkKey, !state.isSelected) }
        )
    }
}

@Composable
private fun ExportDeviceKeys(viewModel: ExportViewModel) {
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
            checked = viewModel.exportUiState.exportDeviceKeys,
            onCheckedChange = { viewModel.onExportDeviceKeysToggled(it) }
        )
    }
}