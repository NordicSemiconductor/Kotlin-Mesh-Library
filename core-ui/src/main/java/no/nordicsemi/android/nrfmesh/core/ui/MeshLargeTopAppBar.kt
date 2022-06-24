package no.nordicsemi.android.nrfmesh.core.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MeshLargeTopAppBar(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior,
    showOverflowMenu: Boolean,
    onOverflowMenuClicked: () -> Unit
) {
    LargeTopAppBar(
        title = { Text(text = title) },
        actions = {
            if (showOverflowMenu)
                IconButton(onClick = {
                    onOverflowMenuClicked()
                }) {
                    Icon(imageVector = Icons.Outlined.MoreVert, contentDescription = null)
                }
        },
        scrollBehavior = scrollBehavior
    )
}