package no.nordicsemi.android.nrfmesh.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.theme.nordicDarkGray

@Composable
fun MeshNodeItem(
    nodeName: String,
    addressHex: String,
    onClick: () -> Unit,
) {
    OutlinedCard(onClick = onClick) {
        Row(
            modifier = Modifier.height(height = 80.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.size(16.dp))
            Image(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.nordicDarkGray,
                        shape = CircleShape
                    )
                    .padding(5.dp),
                painter = painterResource(R.drawable.ic_mesh_white),
                contentDescription = null
            )
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = nodeName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Address: $addressHex",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}