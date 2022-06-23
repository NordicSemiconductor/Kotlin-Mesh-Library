package no.nordicsemi.android.nrfmesh.core.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun MeshLargeTopAppBar(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior,
    showActions: Boolean
) {
    LargeTopAppBar(
        title = { Text(text = title) },
        actions = {
            if (showActions)
                IconButton(onClick = { }) {
                    Icon(imageVector = Icons.Outlined.MoreVert, contentDescription = null)
                }
        },
        scrollBehavior = scrollBehavior
    )
}