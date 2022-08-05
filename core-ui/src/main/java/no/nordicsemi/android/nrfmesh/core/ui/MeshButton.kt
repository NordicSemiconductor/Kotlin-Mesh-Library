package no.nordicsemi.android.nrfmesh.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MeshButton(text: String, onClick: () -> Unit) {
    Button(onClick = { onClick() }) {
        Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
        Text(modifier = Modifier.padding(all = 8.dp), text = text)
    }
}