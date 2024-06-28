package no.nordicsemi.android.nrfmesh.feature.settings

import android.content.ContentResolver
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.Instant
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItemTextField
import no.nordicsemi.android.nrfmesh.core.ui.MeshDropDown
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.kotlin.mesh.core.model.IvIndex
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import java.text.DateFormat
import java.util.Date

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel,
    navigateToProvisioners: () -> Unit,
    navigateToNetworkKeys: () -> Unit,
    navigateToApplicationKeys: () -> Unit,
    navigateToScenes: () -> Unit,
) {
    val uiState: SettingsScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(
        networkState = uiState.networkState,
        importNetwork = { uri, contentResolver ->
            viewModel.importNetwork(uri = uri, contentResolver = contentResolver)
        },
        onNameChanged = viewModel::onNameChanged,
        onProvisionersClicked = navigateToProvisioners,
        onNetworkKeysClicked = navigateToNetworkKeys,
        onApplicationKeysClicked = navigateToApplicationKeys,
        onScenesClicked = navigateToScenes
    )
}

@Composable
fun SettingsScreen(
    networkState: MeshNetworkState,
    importNetwork: (Uri, ContentResolver) -> Unit,
    onNameChanged: (String) -> Unit,
    onProvisionersClicked: () -> Unit,
    onNetworkKeysClicked: () -> Unit,
    onApplicationKeysClicked: () -> Unit,
    onScenesClicked: () -> Unit
) {
    LazyColumn {
        when (networkState) {
            is MeshNetworkState.Success -> {
                settingsInfo(
                    network = networkState.network,
                    onNameChanged = onNameChanged,
                    onProvisionersClicked = onProvisionersClicked,
                    onNetworkKeysClicked = onNetworkKeysClicked,
                    onApplicationKeysClicked = onApplicationKeysClicked,
                    onScenesClicked = onScenesClicked
                )
            }

            is MeshNetworkState.Loading -> {}
            is MeshNetworkState.Error -> {}
        }
    }
}

private fun LazyListScope.settingsInfo(
    network: MeshNetwork,
    onNameChanged: (String) -> Unit,
    onProvisionersClicked: () -> Unit,
    onNetworkKeysClicked: () -> Unit,
    onApplicationKeysClicked: () -> Unit,
    onScenesClicked: () -> Unit
) {
    item {
        SectionTitle(title = stringResource(R.string.label_configuration))
        Spacer(modifier = Modifier.size(8.dp))
    }
    item {
        NetworkNameRow(name = network.name, onNameChanged = onNameChanged)
        Spacer(modifier = Modifier.size(8.dp))
    }
    item {
        ProvisionersRow(
            count = network.provisioners.size,
            onProvisionersClicked = onProvisionersClicked
        )
        Spacer(modifier = Modifier.size(8.dp))
    }
    item {
        NetworkKeysRow(
            count = network.networkKeys.size,
            onNetworkKeysClicked = onNetworkKeysClicked
        )
        Spacer(modifier = Modifier.size(8.dp))
    }
    item {
        ApplicationKeysRow(
            count = network.applicationKeys.size,
            onApplicationKeysClicked = onApplicationKeysClicked
        )
        Spacer(modifier = Modifier.size(8.dp))
    }
    item {
        ScenesRow(
            count = network.scenes.size,
            onScenesClicked = onScenesClicked
        )
        Spacer(modifier = Modifier.size(8.dp))
    }
    item {
        IvIndexRow(ivIndex = network.ivIndex)
        Spacer(modifier = Modifier.size(8.dp))
    }
    item {
        LastModifiedTimeRow(timestamp = network.timestamp)
        Spacer(modifier = Modifier.size(8.dp))
    }
    item { SectionTitle(title = stringResource(R.string.label_about)) }
    item {
        VersionNameRow()
        Spacer(modifier = Modifier.size(8.dp))
    }
    item {
        VersionCodeRow()
        Spacer(modifier = Modifier.size(8.dp))
    }
}

@Composable
private fun NetworkNameRow(name: String, onNameChanged: (String) -> Unit) {
    ElevatedCardItemTextField(
        modifier = Modifier.padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.Badge,
        title = stringResource(id = R.string.label_name),
        subtitle = name,
        placeholder = stringResource(id = R.string.label_placeholder_network_name),
        onValueChanged = onNameChanged
    )
}

@Composable
private fun ProvisionersRow(count: Int, onProvisionersClicked: () -> Unit) {
    ElevatedCardItem(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable { onProvisionersClicked() },
        imageVector = Icons.Outlined.Groups,
        title = stringResource(R.string.label_provisioners),
        subtitle = "$count ${if (count == 1) "provisioner" else "provisioners"} available"
    )
}

@Composable
private fun NetworkKeysRow(count: Int, onNetworkKeysClicked: () -> Unit) {
    ElevatedCardItem(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable { onNetworkKeysClicked() },
        imageVector = Icons.Outlined.VpnKey,
        title = stringResource(R.string.label_network_keys),
        subtitle = "$count ${if (count == 1) "key" else "keys"} available"
    )
}

@Composable
private fun ApplicationKeysRow(count: Int, onApplicationKeysClicked: () -> Unit) {
    ElevatedCardItem(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable { onApplicationKeysClicked() },
        imageVector = Icons.Outlined.VpnKey,
        title = stringResource(R.string.label_application_keys),
        subtitle = "$count ${if (count == 1) "key" else "keys"} available"
    )
}

@Composable
private fun ScenesRow(count: Int, onScenesClicked: () -> Unit) {
    ElevatedCardItem(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable { onScenesClicked() },
        imageVector = Icons.Outlined.AutoAwesome,
        title = stringResource(R.string.label_scenes),
        subtitle = "$count ${if (count == 1) "scene" else "scenes"} available"
    )
}

@Composable
private fun IvIndexRow(ivIndex: IvIndex) {
    ElevatedCardItem(
        modifier = Modifier
            .padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.Tune,
        title = stringResource(R.string.label_iv_index),
        subtitle = "${ivIndex.index}"
    )
}

@Composable
private fun LastModifiedTimeRow(timestamp: Instant) {
    ElevatedCardItem(
        modifier = Modifier
            .padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.Update,
        title = stringResource(R.string.label_last_modified),
        subtitle = DateFormat.getDateTimeInstance().format(
            Date(timestamp.toEpochMilliseconds())
        )
    )
}

@Composable
private fun VersionNameRow() {
    // TODO Clarify version naming
    ElevatedCardItem(
        modifier = Modifier
            .padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.Subtitles,
        title = stringResource(R.string.label_version),
        subtitle = BuildConfig.VERSION_NAME
    )
}

@Composable
private fun VersionCodeRow() {
    // TODO Clarify version code
    ElevatedCardItem(
        modifier = Modifier
            .padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.DataObject,
        title = stringResource(R.string.label_version_code),
        subtitle = BuildConfig.VERSION_CODE
    )
}

@Composable
fun SettingsDropDown(
    navigate: () -> Unit,
    isOptionsMenuExpanded: Boolean,
    onDismiss: () -> Unit,
    importNetwork: () -> Unit,
    resetNetwork: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.TopEnd)
    ) {
        MeshDropDown(
            expanded = isOptionsMenuExpanded,
            onDismiss = { onDismiss() }) {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(imageVector = Icons.Rounded.FileUpload, contentDescription = null)
                },
                text = {
                    Text(
                        text = stringResource(R.string.label_import),
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                onClick = importNetwork
            )
            DropdownMenuItem(
                leadingIcon = {
                    Icon(imageVector = Icons.Rounded.FileDownload, contentDescription = null)
                },
                text = {
                    Text(
                        text = stringResource(R.string.label_export),
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                onClick = navigate
            )
            //MenuDefaults.Divider()
            DropdownMenuItem(
                leadingIcon = {
                    Icon(imageVector = Icons.Rounded.LockReset, contentDescription = null)
                },
                text = {
                    Text(
                        text = stringResource(R.string.label_reset),
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                onClick = resetNetwork
            )
        }
    }
}