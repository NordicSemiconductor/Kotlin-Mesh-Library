@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.feature.export

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedIconToggleButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.ui.MeshTwoLineListItem
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.core.ui.showSnackbar
import no.nordicsemi.android.nrfmesh.feature.export.navigation.ExportScreen
import no.nordicsemi.kotlin.data.toHexString
import no.nordicsemi.kotlin.mesh.core.exception.AtLeastOneNetworkKeyMustBeSelected
import no.nordicsemi.kotlin.mesh.core.exception.AtLeastOneProvisionerMustBeSelected
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.Provisioner


@Composable
fun ExportRoute(
    appState: AppState,
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
    val createDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(stringResource(R.string.document_type)),
        onResult = { it?.let { onExportClicked(context.contentResolver, it) } }
    )
    val screen = appState.currentScreen as? ExportScreen
    LaunchedEffect(key1 = screen) {
        screen?.buttons?.onEach {
            when (it) {
                ExportScreen.Actions.BACK -> onBackPressed()
                ExportScreen.Actions.SAVE -> createDocument.launch(uiState.networkName)
            }
        }?.launchIn(this)
    }
    ExportScreen(
        context = context,
        snackbarHostState = appState.snackbarHostState,
        uiState = uiState,
        onExportEverythingToggled = onExportEverythingToggled,
        onNetworkKeySelected = onNetworkKeySelected,
        onProvisionerSelected = onProvisionerSelected,
        onExportDeviceKeysToggled = onExportDeviceKeysToggled,
        onExportStateDisplayed = onExportStateDisplayed,
    )
}

@Composable
private fun ExportScreen(
    context: Context,
    snackbarHostState: SnackbarHostState,
    uiState: ExportScreenUiState,
    onExportEverythingToggled: (Boolean) -> Unit,
    onNetworkKeySelected: (NetworkKey, Boolean) -> Unit,
    onProvisionerSelected: (Provisioner, Boolean) -> Unit,
    onExportDeviceKeysToggled: (Boolean) -> Unit,
    onExportStateDisplayed: () -> Unit,
) {
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
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        ExportSelection(
            uiState = uiState,
            onExportEverythingToggled = onExportEverythingToggled
        )
        if (uiState.exportEverything) {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                imageVector = Icons.Outlined.Info,
                contentDescription = null
            )
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(id = R.string.label_export_configuration_rationale)
            )
        } else {
            uiState.apply {
                SectionTitle(title = stringResource(R.string.label_provisioners))
                provisionerItemStates.forEach { state ->
                    ProvisionerRow(
                        state = state,
                        onProvisionerSelected = onProvisionerSelected,
                        exportEverything = uiState.exportEverything
                    )
                }
                SectionTitle(title = stringResource(R.string.label_network_keys))
                networkKeyItemStates.forEach { state ->
                    NetworkKeyRow(
                        state = state,
                        onNetworkKeySelected = onNetworkKeySelected,
                        exportEverything = uiState.exportEverything
                    )
                }
            }
            ExportDeviceKeys(
                uiState = uiState,
                onExportDeviceKeysToggled = onExportDeviceKeysToggled
            )
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
        MeshTwoLineListItem(
            modifier = Modifier.weight(1f),
            imageVector = Icons.Outlined.Groups,
            title = state.provisioner.name,
            subtitle = state.provisioner.node?.primaryUnicastAddress?.address?.toString() ?: ""
        )
        VerticalDivider(modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp))
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
            .height(intrinsicSize = IntrinsicSize.Min)
            .fillMaxWidth()
            .padding(end = 16.dp)
            .clickable { onNetworkKeySelected(state.networkKey, !state.isSelected) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        MeshTwoLineListItem(
            modifier = Modifier.weight(1f),
            imageVector = Icons.Outlined.VpnKey,
            title = state.networkKey.name,
            subtitle = state.networkKey.key.toHexString(prefixOx = true),
            subtitleTextOverflow = TextOverflow.Ellipsis
        )
        VerticalDivider(modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp))
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
        VerticalDivider(modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp))
        Switch(
            checked = uiState.exportDeviceKeys,
            onCheckedChange = { onExportDeviceKeysToggled(it) },
            enabled = !uiState.exportEverything
        )
    }
}