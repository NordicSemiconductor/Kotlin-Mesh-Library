package no.nordicsemi.android.nrfmesh.core.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MeshDropDown(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    DropdownMenu(
        modifier = modifier.fillMaxWidth(112.dp/280.dp),
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        content()
    }
}