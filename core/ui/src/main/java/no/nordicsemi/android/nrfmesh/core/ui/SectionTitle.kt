package no.nordicsemi.android.nrfmesh.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun SectionTitle(title: String, style: TextStyle = MaterialTheme.typography.labelLarge) {
    Text(
        modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
        text = title,
        style = style
    )
}