package no.nordicsemi.android.nrfmesh.feature.developer

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.nrfmesh.core.data.DeveloperSettings
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem

@Composable
internal fun DeveloperSettingsScreen(developerSettings: DeveloperSettings) {

}

@Composable
private fun QuickProvisioning() {

}

@Composable
private fun AlwaysReconfigure() {

}


@Composable
private fun QuickProvisioning(
    quickProvisioning: Boolean,
    onQuickProvisioningEnabled: (Boolean) -> Unit
) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Block,
        title = stringResource(R.string.label_quick_provisioning),
        titleAction = {
            Switch(
                modifier = Modifier.padding(horizontal = 16.dp),
                checked = quickProvisioning,
                onCheckedChange = onQuickProvisioningEnabled
            )
        },
        supportingText = ""/*stringResource(R.string.label_quick_provisioning_rationale)*/,
        body = { Spacer(modifier = Modifier.size(8.dp)) }
    )
}