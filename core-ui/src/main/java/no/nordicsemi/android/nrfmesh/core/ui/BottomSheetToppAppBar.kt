package no.nordicsemi.android.nrfmesh.core.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun BottomSheetTopAppBar(
    navigationIcon: ImageVector,
    onNavigationIconClick: () -> Unit,
    title: String,
    titleStyle: TextStyle = MaterialTheme.typography.titleLarge,
    actions: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigationIconClick) {
            Icon(imageVector = navigationIcon, contentDescription = null)
        }
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            style = titleStyle,
        )
        Spacer(modifier = Modifier.size(16.dp))
        actions()
    }
}