package no.nordicsemi.android.nrfmesh.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.PackageInfoCompat
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.nrfmesh.core.ui.RowItem

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    LazyColumn {
        item {
            SettingsSection(viewModel = viewModel)
        }
        item {
            AboutSection()
        }
    }
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