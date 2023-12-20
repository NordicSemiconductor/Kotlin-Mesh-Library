package no.nordicsemi.android.nrfmesh.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ElevatedCardItem(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    title: String,
    titleAction: @Composable () -> Unit = {},
    subtitle: String = "",
    supportingText: String? = null,
    actions: @Composable (RowScope?.() -> Unit)? = null,
) {
    ElevatedCard(modifier = modifier) {
        MeshTwoLineListItem(
            leadingComposable = {
                Icon(
                    modifier = Modifier.padding(end = 16.dp),
                    imageVector = imageVector,
                    contentDescription = null,
                    tint = LocalContentColor.current.copy(alpha = 0.6f)
                )
            },
            title = title,
            subtitle = subtitle,
            trailingComposable = titleAction
        )
        if (supportingText != null)
            Text(
                modifier = Modifier.padding(start = 58.dp, end = 16.dp, bottom = 16.dp),
                text = supportingText,
                style = MaterialTheme.typography.bodyMedium
            )
        actions?.let {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                it()
            }
        }
    }
}