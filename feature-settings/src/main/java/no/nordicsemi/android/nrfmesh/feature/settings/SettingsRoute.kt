package no.nordicsemi.android.nrfmesh.feature.settings

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.Edit
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.Instant
import no.nordicsemi.android.nrfmesh.core.ui.MeshDropDown
import no.nordicsemi.android.nrfmesh.core.ui.MeshOutlinedTextField
import no.nordicsemi.android.nrfmesh.core.ui.MeshTwoLineListItem
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.kotlin.mesh.core.model.IvIndex
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import java.text.DateFormat
import java.util.Date

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = hiltViewModel(),
    navigateToProvisioners: () -> Unit,
    navigateToNetworkKeys: () -> Unit,
    navigateToApplicationKeys: () -> Unit,
    navigateToScenes: () -> Unit,
    navigateToExportNetwork: () -> Unit
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
        onScenesClicked = navigateToScenes,
        onExportClicked = navigateToExportNetwork
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
    onScenesClicked: () -> Unit,
    onExportClicked: () -> Unit
) {
    val context = LocalContext.current
    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { importNetwork(uri, context.contentResolver) } }

    var isOptionsMenuExpanded by rememberSaveable { mutableStateOf(false) }
    LazyColumn {
        when (networkState) {
            is MeshNetworkState.Success -> {
                settingsInfo(
                    context = context,
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
    SettingsDropDown(
        navigate = {
            isOptionsMenuExpanded = !isOptionsMenuExpanded
            onExportClicked()
        },
        isOptionsMenuExpanded = isOptionsMenuExpanded,
        onDismiss = { isOptionsMenuExpanded = !isOptionsMenuExpanded },
        importNetwork = {
            isOptionsMenuExpanded = !isOptionsMenuExpanded
            fileLauncher.launch("application/json")
        }
    )
}

private fun LazyListScope.settingsInfo(
    context: Context, network: MeshNetwork,
    onNameChanged: (String) -> Unit,
    onProvisionersClicked: () -> Unit,
    onNetworkKeysClicked: () -> Unit,
    onApplicationKeysClicked: () -> Unit,
    onScenesClicked: () -> Unit
) {
    item { SectionTitle(title = stringResource(R.string.label_configuration)) }
    item { NetworkNameRow(name = network.name, onNameChanged = onNameChanged) }
    item {
        ProvisionersRow(
            count = network.provisioners.size,
            onProvisionersClicked = onProvisionersClicked
        )
    }
    item {
        NetworkKeysRow(
            count = network.networkKeys.size,
            onNetworkKeysClicked = onNetworkKeysClicked
        )
    }
    item {
        ApplicationKeysRow(
            count = network.applicationKeys.size,
            onApplicationKeysClicked = onApplicationKeysClicked
        )
    }
    item {
        ScenesRow(
            count = network.scenes.size,
            onScenesClicked = onScenesClicked
        )
    }
    item { IvIndexRow(ivIndex = network.ivIndex) }
    item { LastModifiedTimeRow(timestamp = network.timestamp) }
    item { SectionTitle(title = stringResource(R.string.label_about)) }
    item { VersionNameRow() }
    item { VersionCodeRow() }
}

@Composable
private fun NetworkNameRow(name: String, onNameChanged: (String) -> Unit) {
    var value by rememberSaveable { mutableStateOf(name) }
    var onEditClick by rememberSaveable { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            modifier = Modifier.padding(all = 12.dp),
            imageVector = Icons.Outlined.Badge,
            contentDescription = null,
            tint = LocalContentColor.current.copy(alpha = 0.6f)
        )
        Crossfade(targetState = onEditClick) { state ->
            when (state) {
                true -> MeshOutlinedTextField(
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
                    onFocus = onEditClick,
                    value = value,
                    onValueChanged = { value = it },
                    label = { Text(text = stringResource(id = R.string.label_name)) },
                    placeholder = {
                        Text(text = stringResource(id = R.string.label_placeholder_network_name))
                    },
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
                    modifier = Modifier.padding(horizontal = 8.dp),
                    title = stringResource(id = R.string.label_name),
                    subtitle = value,
                    trailingComposable = {
                        IconButton(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            onClick = {
                                onEditClick = !onEditClick
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

@Composable
private fun ProvisionersRow(count: Int, onProvisionersClicked: () -> Unit) {
    MeshTwoLineListItem(
        modifier = Modifier.clickable(onClick = { onProvisionersClicked() }),
        leadingComposable = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.Groups,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        title = stringResource(R.string.label_provisioners),
        subtitle = "$count"
    )
}

@Composable
private fun NetworkKeysRow(count: Int, onNetworkKeysClicked: () -> Unit) {
    MeshTwoLineListItem(
        modifier = Modifier.clickable(onClick = { onNetworkKeysClicked() }),
        leadingComposable = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.VpnKey,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        title = stringResource(R.string.label_network_keys),
        subtitle = "$count"
    )
}

@Composable
private fun ApplicationKeysRow(count: Int, onApplicationKeysClicked: () -> Unit) {
    MeshTwoLineListItem(
        modifier = Modifier.clickable(onClick = { onApplicationKeysClicked() }),
        leadingComposable = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.VpnKey,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        title = stringResource(R.string.label_application_keys),
        subtitle = "$count"
    )
}

@Composable
private fun ScenesRow(count: Int, onScenesClicked: () -> Unit) {
    MeshTwoLineListItem(
        modifier = Modifier.clickable(onClick = { onScenesClicked() }),
        leadingComposable = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        title = stringResource(R.string.label_scenes),
        subtitle = "$count"
    )
}

@Composable
private fun IvIndexRow(ivIndex: IvIndex) {
    MeshTwoLineListItem(
        leadingComposable = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.Tune,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        title = stringResource(R.string.label_iv_index),
        subtitle = "${ivIndex.index}"
    )
}

@Composable
private fun LastModifiedTimeRow(timestamp: Instant) {
    MeshTwoLineListItem(
        leadingComposable = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.Update,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        title = stringResource(R.string.label_last_modified),
        subtitle = DateFormat.getDateTimeInstance().format(
            Date(timestamp.toEpochMilliseconds())
        )
    )
}

@Composable
private fun VersionNameRow() {
    // TODO Clarify version naming
    MeshTwoLineListItem(
        modifier = Modifier.clickable(onClick = { }),
        leadingComposable = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.Subtitles,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        title = stringResource(R.string.label_version),
        subtitle = BuildConfig.VERSION_NAME
    )
}

@Composable
private fun VersionCodeRow() {
    // TODO Clarify version code
    MeshTwoLineListItem(
        modifier = Modifier
            .clickable(onClick = { }),
        leadingComposable = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.DataObject,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        title = stringResource(R.string.label_version_code),
        subtitle = BuildConfig.VERSION_CODE
    )
}

@Composable
fun SettingsDropDown(
    navigate: () -> Unit,
    isOptionsMenuExpanded: Boolean,
    onDismiss: () -> Unit,
    importNetwork: () -> Unit
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
                onClick = {
                    importNetwork()
                }
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
                onClick = {
                    navigate()
                }
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
                onClick = { onDismiss() }
            )
        }
    }
}