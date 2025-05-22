@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
fun MeshTwoLineListItem(
    modifier: Modifier = Modifier,
    leadingComposable: @Composable () -> Unit = {},
    title: String,
    titleTextOverflow: TextOverflow = TextOverflow.Ellipsis,
    subtitle: String? = null,
    trailingComposable: @Composable () -> Unit = {},
    subtitleMaxLines: Int = 1,
    subtitleTextOverflow: TextOverflow = TextOverflow.Ellipsis,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height = 80.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leadingComposable()
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = titleTextOverflow
            )
            if (!subtitle.isNullOrEmpty())
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = subtitleMaxLines,
                    overflow = subtitleTextOverflow
                )
        }
        trailingComposable()
    }
}

@Composable
fun MeshTwoLineListItem(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    title: String,
    subtitle: String? = null,
    subtitleMaxLines: Int = 1,
    subtitleTextOverflow: TextOverflow = TextOverflow.Clip,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height = 80.dp),
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
            modifier = Modifier
                .padding(vertical = 28.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, maxLines = 1)
            if (!subtitle.isNullOrEmpty())
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
fun TwoLineRangeListItem(
    modifier: Modifier = Modifier,
    leadingComposable: @Composable () -> Unit = {},
    title: String,
    titleTextOverflow: TextOverflow = TextOverflow.Clip,
    lineTwo: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier
            .height(height = 80.dp)
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leadingComposable()
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = titleTextOverflow
            )
            lineTwo()
        }
    }
}


@Composable
fun SingleLineRangeListItem(
    modifier: Modifier = Modifier,
    leadingComposable: @Composable () -> Unit = {},
    title: String,
    titleTextOverflow: TextOverflow = TextOverflow.Clip,
    trailingComposable: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier
            .padding(vertical = 16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leadingComposable()
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp),
            text = title,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = titleTextOverflow
        )
        trailingComposable()
    }
}