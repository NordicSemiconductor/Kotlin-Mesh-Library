@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLifecycleComposeApi::class
)

package no.nordicsemi.android.nrfmesh.feature.settings

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.PackageInfoCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.Instant
import no.nordicsemi.android.nrfmesh.core.ui.MeshDropDown
import no.nordicsemi.android.nrfmesh.core.ui.MeshLargeTopAppBar
import no.nordicsemi.android.nrfmesh.core.ui.MeshTwoLineListItem
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.kotlin.mesh.core.model.IvIndex
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import java.text.DateFormat
import java.util.*

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = hiltViewModel(),
    navigateToNetworkKeys: () -> Unit,
    navigateToApplicationKeys: () -> Unit,
    navigateToExportNetwork: () -> Unit
) {
    val uiState: SettingsScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(
        networkState = uiState.networkState,
        importNetwork = { uri, contentResolver ->
            viewModel.importNetwork(uri = uri, contentResolver = contentResolver)
        },
        onNetworkKeysClicked = navigateToNetworkKeys,
        onApplicationKeysClicked = navigateToApplicationKeys,
        onExportClicked = navigateToExportNetwork
    )
}

@Composable
fun SettingsScreen(
    networkState: MeshNetworkState,
    importNetwork: (Uri, ContentResolver) -> Unit,
    onNetworkKeysClicked: () -> Unit,
    onApplicationKeysClicked: () -> Unit,
    onExportClicked: () -> Unit
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { importNetwork(uri, context.contentResolver) } }

    var isOptionsMenuExpanded by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        modifier = Modifier.nestedScroll(connection = scrollBehavior.nestedScrollConnection),
        topBar = {
            MeshLargeTopAppBar(
                title = "Network",
                actions = {
                    IconButton(onClick = { isOptionsMenuExpanded = !isOptionsMenuExpanded }) {
                        Icon(imageVector = Icons.Outlined.MoreVert, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        content = { padding ->
            LazyColumn(
                contentPadding = padding
            ) {
                when (networkState) {
                    is MeshNetworkState.Success -> {
                        settingsInfo(
                            context = context,
                            network = networkState.network,
                            onNetworkKeysClicked = onNetworkKeysClicked,
                            onApplicationKeysClicked = onApplicationKeysClicked
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
    )
}

private fun LazyListScope.settingsInfo(
    context: Context, network: MeshNetwork,
    onNetworkKeysClicked: () -> Unit,
    onApplicationKeysClicked: () -> Unit,
) {
    item { SectionTitle(title = stringResource(R.string.label_configuration)) }
    item { NetworkNameRow(name = network.name) }
    item { ProvisionersRow(count = network.provisioners.size) }
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
    item { ScenesRow(count = network.scenes.size) }
    item { IvIndexRow(ivIndex = network.ivIndex) }
    item { LastModifiedTimeRow(timestamp = network.timestamp) }
    item { SectionTitle(title = stringResource(R.string.label_about)) }
    item { VersionNameRow(context = context) }
    item { VersionCodeRow(context = context) }
}

@Composable
fun NetworkNameRow(name: String) {
    MeshTwoLineListItem(
        modifier = Modifier.clickable(onClick = { }),
        leadingIcon = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.Badge,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        title = stringResource(R.string.label_name),
        subtitle = name
    )
}

@Composable
fun ProvisionersRow(count: Int) {
    MeshTwoLineListItem(
        modifier = Modifier.clickable(onClick = { }),
        leadingIcon = {
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
fun NetworkKeysRow(count: Int, onNetworkKeysClicked: () -> Unit) {
    MeshTwoLineListItem(
        modifier = Modifier.clickable(onClick = { onNetworkKeysClicked() }),
        leadingIcon = {
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
fun ApplicationKeysRow(count: Int, onApplicationKeysClicked: () -> Unit) {
    MeshTwoLineListItem(
        modifier = Modifier.clickable(onClick = { onApplicationKeysClicked() }),
        leadingIcon = {
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
fun ScenesRow(count: Int) {
    MeshTwoLineListItem(
        modifier = Modifier.clickable(onClick = { }),
        leadingIcon = {
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
fun IvIndexRow(ivIndex: IvIndex) {
    MeshTwoLineListItem(
        modifier = Modifier.clickable(onClick = { }),
        leadingIcon = {
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
fun LastModifiedTimeRow(timestamp: Instant) {
    MeshTwoLineListItem(
        modifier = Modifier.clickable(onClick = { }),
        leadingIcon = {
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
fun VersionNameRow(context: Context) {
    // TODO Clarify version naming
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    MeshTwoLineListItem(
        modifier = Modifier.clickable(onClick = { }),
        leadingIcon = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.Subtitles,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        title = stringResource(R.string.label_version),
        subtitle = packageInfo.versionName
    )
}

@Composable
fun VersionCodeRow(context: Context) {
    // TODO Clarify version code
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    MeshTwoLineListItem(
        modifier = Modifier
            .clickable(onClick = { }),
        leadingIcon = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.DataObject,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        title = stringResource(R.string.label_version_code),
        subtitle = "${PackageInfoCompat.getLongVersionCode(packageInfo)}"
    )
}

@Composable
internal fun SettingsDropDown(
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