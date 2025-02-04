package no.nordicsemi.android.nrfmesh.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItemTextField
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.kotlin.mesh.core.model.IvIndex
import java.text.DateFormat
import java.util.Date

@Composable
internal fun SettingsList(
    settingsListData: SettingsListData,
    selectedSetting: ClickableSetting?,
    highlightSelectedItem: Boolean,
    onNameChanged: (String) -> Unit,
    navigateToProvisioners: () -> Unit,
    navigateToNetworkKeys: () -> Unit,
    navigateToApplicationKeys: () -> Unit,
    navigateToScenes: () -> Unit
) {
    SettingsScreen(
        settingsListData = settingsListData,
        selectedSetting = selectedSetting,
        highlightSelectedItem = highlightSelectedItem,
        onNameChanged = onNameChanged,
        onProvisionersClicked = navigateToProvisioners,
        onNetworkKeysClicked = navigateToNetworkKeys,
        onApplicationKeysClicked = navigateToApplicationKeys,
        onScenesClicked = navigateToScenes
    )
}

@Composable
private fun SettingsScreen(
    settingsListData: SettingsListData,
    selectedSetting: ClickableSetting?,
    highlightSelectedItem: Boolean,
    onNameChanged: (String) -> Unit,
    onProvisionersClicked: () -> Unit,
    onNetworkKeysClicked: () -> Unit,
    onApplicationKeysClicked: () -> Unit,
    onScenesClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
    ) {
        SectionTitle(
            modifier = Modifier.padding(vertical = 8.dp),
            title = stringResource(R.string.label_configuration)
        )
        NetworkNameRow(name = settingsListData.name, onNameChanged = onNameChanged)
        ProvisionersRow(
            count = settingsListData.provisionerCount,
            isSelected = selectedSetting == ClickableSetting.Provisioners && highlightSelectedItem,
            onProvisionersClicked = onProvisionersClicked
        )
        NetworkKeysRow(
            count = settingsListData.networkKeyCount,
            isSelected = selectedSetting == ClickableSetting.NetworkKeys && highlightSelectedItem,
            onNetworkKeysClicked = onNetworkKeysClicked
        )
        ApplicationKeysRow(
            count = settingsListData.appKeyCount,
            isSelected = selectedSetting == ClickableSetting.ApplicationKeys && highlightSelectedItem,
            onApplicationKeysClicked = onApplicationKeysClicked
        )
        ScenesRow(
            count = settingsListData.sceneCount,
            isSelected = selectedSetting == ClickableSetting.Scenes && highlightSelectedItem,
            onScenesClicked = onScenesClicked
        )
        IvIndexRow(ivIndex = settingsListData.ivIndex)
        LastModifiedTimeRow(timestamp = settingsListData.timestamp)
        SectionTitle(
            modifier = Modifier.padding(vertical = 8.dp),
            title = stringResource(R.string.label_about)
        )
        VersionNameRow()
        VersionCodeRow()
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
private fun ProvisionersRow(
    count: Int,
    isSelected: Boolean,
    onProvisionersClicked: () -> Unit,
) {
    ElevatedCardItem(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp),
        colors = when (isSelected) {
            true -> CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )

            else -> CardDefaults.outlinedCardColors()
        },
        onClick = onProvisionersClicked,
        imageVector = Icons.Outlined.Groups,
        title = stringResource(R.string.label_provisioners),
        subtitle = "$count ${if (count == 1) "provisioner" else "provisioners"} available"
    )
}

@Composable
private fun NetworkKeysRow(
    count: Int,
    isSelected: Boolean,
    onNetworkKeysClicked: () -> Unit,
) {
    ElevatedCardItem(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp),
        colors = when (isSelected) {
            true -> CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )

            else -> CardDefaults.outlinedCardColors()
        },
        onClick = onNetworkKeysClicked,
        imageVector = Icons.Outlined.VpnKey,
        title = stringResource(R.string.label_network_keys),
        subtitle = "$count ${if (count == 1) "key" else "keys"} available"
    )
}

@Composable
private fun ApplicationKeysRow(
    count: Int,
    isSelected: Boolean,
    onApplicationKeysClicked: () -> Unit,
) {
    ElevatedCardItem(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp),
        colors = when (isSelected) {
            true -> CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )

            else -> CardDefaults.outlinedCardColors()
        },
        onClick = onApplicationKeysClicked,
        imageVector = Icons.Outlined.VpnKey,
        title = stringResource(R.string.label_application_keys),
        subtitle = "$count ${if (count == 1) "key" else "keys"} available"
    )
}

@Composable
private fun ScenesRow(
    count: Int,
    isSelected: Boolean,
    onScenesClicked: () -> Unit,
) {
    ElevatedCardItem(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp),
        colors = when (isSelected) {
            true -> CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )

            else -> CardDefaults.outlinedCardColors()
        },
        onClick = onScenesClicked,
        imageVector = Icons.Outlined.AutoAwesome,
        title = stringResource(R.string.label_scenes),
        subtitle = "$count ${if (count == 1) "scene" else "scenes"} available"
    )
}

@Composable
private fun IvIndexRow(ivIndex: IvIndex) {
    ElevatedCardItem(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp),
        imageVector = Icons.Outlined.Tune,
        title = stringResource(R.string.label_iv_index),
        subtitle = "${ivIndex.index}"
    )
}

@Composable
private fun LastModifiedTimeRow(timestamp: Instant) {
    ElevatedCardItem(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp),
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
            .padding(horizontal = 16.dp)
            .padding(vertical = 8.dp),
        imageVector = Icons.Outlined.DataObject,
        title = stringResource(R.string.label_version_code),
        subtitle = BuildConfig.VERSION_CODE
    )
}