package no.nordicsemi.android.feature.config.networkkeys

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.nrfmesh.core.ui.BottomSheetTopAppBar
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.EmptyNetworkKeysContent
import no.nordicsemi.android.nrfmesh.feature.config.networkkeys.R
import no.nordicsemi.kotlin.data.toHexString
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey


@OptIn(ExperimentalStdlibApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetNetworkKeys(
    title: String,
    keys: List<NetworkKey>,
    onAddKeyClicked: (NetworkKey) -> Unit,
    navigateToNetworkKeys: () -> Unit,
    onDismissClick: () -> Unit,
    emptyKeysContent: @Composable () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismissClick) {
        BottomSheetTopAppBar(
            navigationIcon = Icons.Outlined.Close,
            onNavigationIconClick = onDismissClick,
            title = title
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            if (keys.isEmpty()) {
                item {
                    EmptyNetworkKeysContent(
                        noItemsAvailableContent = emptyKeysContent,
                        onClickText = stringResource(R.string.action_settings),
                        onClick = { navigateToNetworkKeys() }
                    )
                }
            } else {
                items(items = keys) { key ->
                    ElevatedCardItem(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onClick = {
                            onDismissClick()
                            onAddKeyClicked(key)
                        },
                        imageVector = Icons.Outlined.VpnKey,
                        title = key.name,
                        subtitle = key.key.toHexString()
                    )
                }
            }
        }
    }
}
