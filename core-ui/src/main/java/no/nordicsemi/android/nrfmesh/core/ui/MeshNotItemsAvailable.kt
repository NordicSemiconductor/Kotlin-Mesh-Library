package no.nordicsemi.android.nrfmesh.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun MeshNoItemsAvailable(imageVector: ImageVector, rationale: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            modifier = Modifier.size(128.dp),
            imageVector = imageVector,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.surfaceTint
        )
        Text(
            modifier = Modifier.padding(vertical = 16.dp),
            text = rationale
        )
    }
}