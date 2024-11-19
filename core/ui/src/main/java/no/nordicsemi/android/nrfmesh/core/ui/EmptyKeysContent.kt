package no.nordicsemi.android.nrfmesh.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun EmptyKeysContent(
    noItemsAvailableContent: @Composable () -> Unit,
    onClickText: String,
    onClick: () -> Unit
) {
    noItemsAvailableContent()
    MeshNoItemsAvailable(
        imageVector = Icons.Outlined.VpnKey,
        title = stringResource(R.string.label_no_keys_added)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        OutlinedButton(
            onClick = onClick,
            content = { Text(text = onClickText) }
        )
    }
}