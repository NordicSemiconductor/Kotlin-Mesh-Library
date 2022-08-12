@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.feature.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.PackageInfoCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import no.nordicsemi.android.nrfmesh.core.ui.MeshDropDown
import no.nordicsemi.android.nrfmesh.core.ui.MeshLargeTopAppBar
import no.nordicsemi.android.nrfmesh.core.ui.RowItem
import no.nordicsemi.android.nrfmesh.feature.export.navigation.ExportDestination

@Composable
fun SettingsRoute(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    SettingsScreen(navController = navController, viewModel = viewModel)
}

@Composable
fun SettingsScreen(navController: NavController, viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        decayAnimationSpec = rememberSplineBasedDecay(),
        state = rememberTopAppBarState()
    )
    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importNetwork(uri = uri, contentResolver = context.contentResolver) }
    }

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
                item {
                    SettingsSection(viewModel = viewModel)
                }
                item {
                    AboutSection()
                }
            }
            SettingsDropDown(
                navigate = {
                    isOptionsMenuExpanded = !isOptionsMenuExpanded
                    navController.navigate(ExportDestination.destination)
                },
                isOptionsMenuExpanded = isOptionsMenuExpanded,
                onDismiss = { isOptionsMenuExpanded = !isOptionsMenuExpanded },
                importNetwork = { fileLauncher.launch("application/json") }
            )
        }
    )
}

@Composable
fun SettingsSection(viewModel: SettingsViewModel) {
    Text(
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
        text = stringResource(R.string.label_configuration),
        style = MaterialTheme.typography.labelLarge
    )
    RowItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { }),
        imageVector = Icons.Outlined.Badge,
        title = stringResource(R.string.label_name),
        subtitle = viewModel.uiState.networkName
    )
    RowItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { }),
        imageVector = Icons.Outlined.Groups,
        title = stringResource(R.string.label_provisioners),
        subtitle = "${viewModel.uiState.provisioners.size}"
    )
    RowItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { }),
        imageVector = Icons.Outlined.VpnKey,
        title = stringResource(R.string.label_network_keys),
        subtitle = "${viewModel.uiState.networkKeys.size}"
    )
    RowItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { }),
        imageVector = Icons.Outlined.VpnKey,
        title = stringResource(R.string.label_application_keys),
        subtitle = "${viewModel.uiState.applicationKeys.size}"
    )
    RowItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { }),
        imageVector = Icons.Outlined.AutoAwesome,
        title = stringResource(R.string.label_scenes),
        subtitle = "${viewModel.uiState.networkKeys.size}"
    )
    RowItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { }),
        imageVector = Icons.Outlined.Tune,
        title = stringResource(R.string.label_iv_index),
        subtitle = "${viewModel.uiState.ivIndex.index}"
    )
    RowItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { }),
        imageVector = Icons.Outlined.Update,
        title = stringResource(R.string.label_last_modified),
        subtitle = viewModel.uiState.lastModified
    )
}

@Composable
fun AboutSection() {
    val context = LocalContext.current
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    Text(
        modifier = Modifier.padding(start = 16.dp, top = 16.dp),
        text = stringResource(R.string.label_about),
        style = MaterialTheme.typography.labelLarge
    )
    RowItem(
        imageVector = Icons.Outlined.Subtitles,
        title = stringResource(R.string.label_version),
        subtitle = packageInfo.versionName
    )
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