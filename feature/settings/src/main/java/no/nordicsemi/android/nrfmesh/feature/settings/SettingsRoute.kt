package no.nordicsemi.android.nrfmesh.feature.settings

import android.content.ContentResolver
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Instant
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItemTextField
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.feature.settings.navigation.SettingsScreen
import no.nordicsemi.kotlin.mesh.core.model.IvIndex
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import java.text.DateFormat
import java.util.Date

@Composable
fun SettingsRoute(
    appState: AppState,
    uiState: SettingsScreenUiState,
    onNameChanged: (String) -> Unit,
    navigateToProvisioners: () -> Unit,
    navigateToNetworkKeys: () -> Unit,
    navigateToApplicationKeys: () -> Unit,
    navigateToScenes: () -> Unit,
    importNetwork: (uri: Uri, contentResolver: ContentResolver) -> Unit,
    navigateToExport: () -> Unit,
    resetNetwork: () -> Unit
) {
    val context = LocalContext.current
    val screen = appState.currentScreen as? SettingsScreen
    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            importNetwork(uri, context.contentResolver)
        }
    }
    LaunchedEffect(key1 = screen) {
        screen?.buttons?.onEach { button ->
            when (button) {
                SettingsScreen.Actions.IMPORT -> {
                    fileLauncher.launch("application/json")
                }

                SettingsScreen.Actions.EXPORT -> navigateToExport()
                SettingsScreen.Actions.RESET -> resetNetwork()
            }
        }?.launchIn(this)
    }
    SettingsScreen(
        networkState = uiState.networkState,
        onNameChanged = onNameChanged,
        onProvisionersClicked = navigateToProvisioners,
        onNetworkKeysClicked = navigateToNetworkKeys,
        onApplicationKeysClicked = navigateToApplicationKeys,
        onScenesClicked = navigateToScenes
    )
}

@Composable
fun SettingsScreen(
    networkState: MeshNetworkState,
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
        modifier = Modifier.padding(horizontal = 16.dp),
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
        modifier = Modifier.padding(horizontal = 16.dp),
        onClick = { onProvisionersClicked() },
        imageVector = Icons.Outlined.Groups,
        title = stringResource(R.string.label_provisioners),
        subtitle = "$count ${if (count == 1) "provisioner" else "provisioners"} available"
    )
}

@Composable
private fun NetworkKeysRow(count: Int, onNetworkKeysClicked: () -> Unit) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        onClick = { onNetworkKeysClicked() },
        imageVector = Icons.Outlined.VpnKey,
        title = stringResource(R.string.label_network_keys),
        subtitle = "$count ${if (count == 1) "key" else "keys"} available"
    )
}

@Composable
private fun ApplicationKeysRow(count: Int, onApplicationKeysClicked: () -> Unit) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        onClick = { onApplicationKeysClicked() },
        imageVector = Icons.Outlined.VpnKey,
        title = stringResource(R.string.label_application_keys),
        subtitle = "$count ${if (count == 1) "key" else "keys"} available"
    )
}

@Composable
private fun ScenesRow(count: Int, onScenesClicked: () -> Unit) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        onClick = { onScenesClicked() },
        imageVector = Icons.Outlined.AutoAwesome,
        title = stringResource(R.string.label_scenes),
        subtitle = "$count ${if (count == 1) "scene" else "scenes"} available"
    )
}

@Composable
private fun IvIndexRow(ivIndex: IvIndex) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Tune,
        title = stringResource(R.string.label_iv_index),
        subtitle = "${ivIndex.index}"
    )
}

@Composable
private fun LastModifiedTimeRow(timestamp: Instant) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
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
        modifier = Modifier.padding(horizontal = 16.dp),
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