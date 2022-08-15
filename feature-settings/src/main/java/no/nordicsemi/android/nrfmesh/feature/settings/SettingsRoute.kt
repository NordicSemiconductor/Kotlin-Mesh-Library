@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.feature.settings

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.core.content.pm.PackageInfoCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import no.nordicsemi.android.nrfmesh.core.ui.MeshDropDown
import no.nordicsemi.android.nrfmesh.core.ui.MeshLargeTopAppBar
import no.nordicsemi.android.nrfmesh.core.ui.RowItem
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.feature.export.navigation.ExportDestination
import no.nordicsemi.kotlin.mesh.core.model.IvIndex

@Composable
fun SettingsRoute(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    SettingsScreen(
        navController = navController,
        uiState = viewModel.uiState,
        importNetwork = { uri, contentResolver ->
            viewModel.importNetwork(uri = uri, contentResolver = contentResolver)
        })
}

@Composable
fun SettingsScreen(
    navController: NavController,
    uiState: SettingsUiState,
    importNetwork: (Uri, ContentResolver) -> Unit
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        decayAnimationSpec = rememberSplineBasedDecay(),
        state = rememberTopAppBarState()
    )
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
                item { SectionTitle(title = stringResource(R.string.label_configuration)) }
                item { NetworkNameRow(name = uiState.networkName) }
                item { ProvisionersRow(count = uiState.provisioners.size) }
                item { NetworkKeysRow(count = uiState.networkKeys.size) }
                item { ApplicationKeysRow(count = uiState.applicationKeys.size) }
                item { ScenesRow(count = uiState.scenes.size) }
                item { IvIndexRow(ivIndex = uiState.ivIndex) }
                item { LastModifiedTimeRow(timestamp = uiState.lastModified) }
                item { SectionTitle(title = stringResource(R.string.label_about)) }
                item { VersionNameRow(context = context) }
                item { VersionCodeRow(context = context) }
            }
            SettingsDropDown(
                navigate = {
                    isOptionsMenuExpanded = !isOptionsMenuExpanded
                    navController.navigate(ExportDestination.destination)
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

@Composable
fun NetworkNameRow(name: String) {
    RowItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { }),
        imageVector = Icons.Outlined.Badge,
        title = stringResource(R.string.label_name),
        subtitle = name
    )
}

@Composable
fun ProvisionersRow(count: Int) {
    RowItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { }),
        imageVector = Icons.Outlined.Groups,
        title = stringResource(R.string.label_provisioners),
        subtitle = "$count"
    )
}

@Composable
fun NetworkKeysRow(count: Int) {
    RowItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = {

            }),
        imageVector = Icons.Outlined.VpnKey,
        title = stringResource(R.string.label_network_keys),
        subtitle = "$count"
    )
}

@Composable
fun ApplicationKeysRow(count: Int) {
    RowItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { }),
        imageVector = Icons.Outlined.VpnKey,
        title = stringResource(R.string.label_application_keys),
        subtitle = "$count"
    )
}

@Composable
fun ScenesRow(count: Int) {
    RowItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { }),
        imageVector = Icons.Outlined.AutoAwesome,
        title = stringResource(R.string.label_scenes),
        subtitle = "$count"
    )
}

@Composable
fun IvIndexRow(ivIndex: IvIndex) {
    RowItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { }),
        imageVector = Icons.Outlined.Tune,
        title = stringResource(R.string.label_iv_index),
        subtitle = "${ivIndex.index}"
    )
}

@Composable
fun LastModifiedTimeRow(timestamp: String) {
    RowItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { }),
        imageVector = Icons.Outlined.Update,
        title = stringResource(R.string.label_last_modified),
        subtitle = timestamp
    )
}

@Composable
fun VersionNameRow(context: Context) {
    // TODO Clarify version naming
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    RowItem(
        imageVector = Icons.Outlined.Subtitles,
        title = stringResource(R.string.label_version),
        subtitle = packageInfo.versionName
    )
}

@Composable
fun VersionCodeRow(context: Context) {
    // TODO Clarify version code
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    RowItem(
        imageVector = Icons.Outlined.DataObject,
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