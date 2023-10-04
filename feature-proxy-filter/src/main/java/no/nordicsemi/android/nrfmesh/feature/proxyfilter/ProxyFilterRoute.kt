package no.nordicsemi.android.nrfmesh.feature.proxyfilter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.nrfmesh.core.ui.MeshTwoLineListItem

@Composable
internal fun ProxyFilterRoute(viewModel: ProxyFilterViewModel) {
    ProxyFilterScreen()
}

@Composable
private fun ProxyFilterScreen() {
    LazyColumn {
        proxyFilterInfo()
    }
}

private fun LazyListScope.proxyFilterInfo() {
    item { AutomaticConnectionRow() }
    item { ProxyRow() }
}

@Composable
private fun AutomaticConnectionRow() {
    var isChecked by rememberSaveable { mutableStateOf(true) }
    MeshTwoLineListItem(
        modifier = Modifier.clickable(onClick = { }),
        leadingComposable = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        title = "Automatic Connection",
        trailingComposable = {
            Switch(checked = isChecked, onCheckedChange = { isChecked = it })
        }
    )
}

@Composable
private fun ProxyRow() {
    MeshTwoLineListItem(
        modifier = Modifier.clickable(onClick = { }),
        leadingComposable = {
            Icon(
                modifier = Modifier.padding(all = 16.dp),
                imageVector = Icons.Outlined.Hub,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f)
            )
        },
        title = "Proxy",
        subtitle = "No device connected",
        trailingComposable = {
            Text(
                modifier = Modifier.padding(all = 16.dp),
                text = "CONNECT",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    )
}