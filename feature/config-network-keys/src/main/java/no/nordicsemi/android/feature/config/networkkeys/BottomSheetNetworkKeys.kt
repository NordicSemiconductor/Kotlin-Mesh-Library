package no.nordicsemi.android.feature.config.networkkeys

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.android.nrfmesh.core.ui.MeshOutlinedButton
import no.nordicsemi.android.nrfmesh.core.ui.Row
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.feature.config.networkkeys.R
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetNetworkKeys(
    bottomSheetState: SheetState,
    keys: List<NetworkKey>,
    onAddNetworkKeyClicked: () -> Unit,
    onNetworkKeyClicked: (NetworkKey) -> Unit,
    navigateToNetworkKeys: () -> Unit,
    onDismissClick: () -> Unit,
) {
    ModalBottomSheet(sheetState = bottomSheetState, onDismissRequest = onDismissClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitle(
                modifier = Modifier.weight(weight = 1f),
                title = stringResource(R.string.label_network_keys)
            )
            MeshOutlinedButton(
                onClick = onAddNetworkKeyClicked,
                buttonIcon = Icons.Outlined.Add,
                text = stringResource(R.string.label_add_key)
            )
            MeshOutlinedButton(
                onClick = navigateToNetworkKeys,
                buttonIcon = Icons.Outlined.Settings,
                text = stringResource(R.string.label_settings)
            )
        }

        when (keys.isEmpty()) {
            true -> MeshNoItemsAvailable(
                modifier = Modifier.fillMaxSize(),
                imageVector = Icons.Outlined.VpnKey,
                title = stringResource(R.string.label_no_keys_available),
                rationale = stringResource(R.string.label_no_keys_available_rationale),
                onClickText = stringResource(R.string.label_settings),
            )

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(space = 8.dp)
            ) {
                items(items = keys) { key ->
                    key.Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onClick = {
                            onDismissClick()
                            onNetworkKeyClicked(key)
                        }
                    )
                }
            }
        }
    }
}
