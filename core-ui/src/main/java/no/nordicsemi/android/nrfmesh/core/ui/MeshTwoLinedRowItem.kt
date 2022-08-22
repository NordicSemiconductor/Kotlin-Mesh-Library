package no.nordicsemi.android.nrfmesh.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun RowItem(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    title: String,
    subtitle: String = "",
    subtitleMaxLines: Int = 1,
    subtitleTextOverflow: TextOverflow = TextOverflow.Clip
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .padding(start = 28.dp, end = 16.dp)
                .size(24.dp),
            imageVector = imageVector,
            contentDescription = null,
            tint = LocalContentColor.current.copy(alpha = 0.6f)
        )
        Column(
            Modifier
                .padding(vertical = 28.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, maxLines = 1)
            if (subtitle.isNotEmpty())
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = subtitleMaxLines,
                    overflow = subtitleTextOverflow
                )
        }
    }
}

@Composable
fun RowItem(
    modifier: Modifier = Modifier,
    leadingIcon: @Composable () -> Unit = {},
    title: String,
    subtitle: String = "",
    trailingIcon: @Composable () -> Unit = {},
    subtitleMaxLines: Int = 1,
    subtitleTextOverflow: TextOverflow = TextOverflow.Clip
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leadingIcon()
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, maxLines = 1)
            if (subtitle.isNotEmpty())
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = subtitleMaxLines,
                    overflow = subtitleTextOverflow
                )
        }
        trailingIcon()
    }
}