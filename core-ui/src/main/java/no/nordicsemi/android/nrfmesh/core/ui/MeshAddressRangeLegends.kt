package no.nordicsemi.android.nrfmesh.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun AddressRangeLegends() {
    Legend(
        color = Color.White,
        description = stringResource(R.string.not_allocated)
    )
    Legend(
        color = MaterialTheme.colorScheme.primary,
        description = stringResource(R.string.allocated_to_this_provisioner)
    )
    Legend(
        color = Color.DarkGray,
        description = stringResource(R.string.allocated_to_another_provisioner)
    )
    Legend(
        color = Color.Red,
        description = stringResource(R.string.conflicting_with_another_provisioner)
    )
}

@Composable
private fun Legend(color: Color, description: String) {
    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(size = 16.dp)
                .background(color = color, shape = RectangleShape)
        )
        Text(
            modifier = Modifier
                .weight(weight = 2f, fill = true)
                .padding(start = 16.dp),
            text = description,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}