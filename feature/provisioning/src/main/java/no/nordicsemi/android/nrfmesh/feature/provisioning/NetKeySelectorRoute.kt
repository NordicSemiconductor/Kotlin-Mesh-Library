package no.nordicsemi.android.nrfmesh.feature.provisioning

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.nrfmesh.core.ui.MeshTwoLineListItem
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey

@Composable
internal fun NetKeySelectorRoute(
    uiState: NetworkKeySelectionScreenUiState,
    onKeySelected: (KeyIndex) -> Unit
) {
    NetKeySelectorScreen(
        uiState = uiState,
        onKeySelected = onKeySelected
    )
}

@Composable
private fun NetKeySelectorScreen(
    uiState: NetworkKeySelectionScreenUiState,
    onKeySelected: (KeyIndex) -> Unit
) {
    LazyColumn {
        items(items = uiState.keys, key = { it.index.toInt() }) { key ->
            NetKeyItem(
                key = key,
                isSelected = key.index == uiState.selectedKeyIndex,
                onKeySelected = onKeySelected
            )
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun NetKeyItem(key: NetworkKey, isSelected: Boolean, onKeySelected: (KeyIndex) -> Unit) {
    MeshTwoLineListItem(
        modifier = Modifier.clickable {
            onKeySelected(key.index)
        },
        leadingComposable = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.VpnKey,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        }, title = key.name,
        subtitle = key.key.toHexString(),
        trailingComposable = {
            Checkbox(checked = isSelected, onCheckedChange = { if (it) onKeySelected(key.index) })
        }
    )
}
