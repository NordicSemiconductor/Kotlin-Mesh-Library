package no.nordicsemi.android.nrfmesh.feature.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.nrfmesh.core.ui.SettingsRowItem

@Composable
fun SettingsScreen() {
    LazyColumn {
        item {
            SettingsSection()
        }
        item {
            AboutSection()
        }
    }
}

@Composable
fun SettingsSection() {
    Text(
        modifier = Modifier.padding(start = 16.dp, top = 16.dp),
        text = "Configuration",
        style = MaterialTheme.typography.labelLarge
    )
    SettingsRowItem(
        imageVector = Icons.Outlined.Badge,
        title = stringResource(R.string.label_name),
        subtitle = "nRF Mesh"
    ) {

    }
    SettingsRowItem(
        imageVector = Icons.Outlined.Groups,
        title = stringResource(R.string.label_provisioners),
        subtitle = "3 Provisioners"
    ) {

    }
    SettingsRowItem(
        imageVector = Icons.Outlined.VpnKey,
        title = stringResource(R.string.label_network_keys),
        subtitle = ""
    ) {

    }
    SettingsRowItem(
        imageVector = Icons.Outlined.VpnKey,
        title = stringResource(R.string.label_application_keys),
        subtitle = ""
    ) {

    }
    SettingsRowItem(
        imageVector = Icons.Outlined.AutoAwesome,
        title = stringResource(R.string.label_scenes),
        subtitle = ""
    ) {

    }
    SettingsRowItem(
        imageVector = Icons.Outlined.Tune,
        title = stringResource(R.string.label_iv_test_mode),
        subtitle = ""
    ) {

    }
    SettingsRowItem(
        imageVector = Icons.Outlined.Update,
        title = "Last Modified",
        subtitle = ""
    ) {

    }
}

@Composable
fun AboutSection() {
    Text(
        modifier = Modifier.padding(start = 16.dp, top = 16.dp),
        text = "About",
        style = MaterialTheme.typography.labelLarge
    )
    SettingsRowItem(
        imageVector = Icons.Outlined.Update,
        title = "Version",
        subtitle = "3.2.4"
    ) {}
    SettingsRowItem(
        imageVector = Icons.Outlined.Update,
        title = "Version Code",
        subtitle = "102"
    ) {}
}