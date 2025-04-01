package no.nordicsemi.android.nrfmesh.core.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey


/**
 * Composable function that displays a key item with an icon, title, and optional subtitle and
 * supporting text.
 */
@Composable
fun Key(
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.outlinedCardColors(),
    enabled: Boolean = true,
    onClick: () -> Unit,
    imageVector: ImageVector = Icons.Outlined.VpnKey,
    title: String,
    titleAction: @Composable () -> Unit = {},
    subtitle: String? = null,
    supportingText: String? = null,
    actions: @Composable (RowScope?.() -> Unit)? = null,
) {
    ElevatedCardItem(
        modifier = modifier.padding(horizontal = 16.dp),
        colors = colors,
        enabled = enabled,
        onClick = onClick,
        imageVector = imageVector,
        title = title,
        titleAction = titleAction,
        subtitle = subtitle,
        supportingText = supportingText,
        actions = actions
    )
}

@Composable
fun NetworkKey.Row(
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.outlinedCardColors(),
    enabled: Boolean = true,
    onClick: () -> Unit,
    imageVector: ImageVector = Icons.Outlined.VpnKey,
) {
    ElevatedCardItem(
        modifier = modifier,
        colors = colors,
        enabled = enabled,
        onClick = onClick,
        imageVector = imageVector,
        title = name,
        subtitle = null,
        supportingText = null,
        actions = null
    )
}

@Composable
fun ApplicationKey.Row(
    colors: CardColors = CardDefaults.outlinedCardColors(),
    enabled: Boolean = true,
    onClick: () -> Unit,
    imageVector: ImageVector = Icons.Outlined.VpnKey,
) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        colors = colors,
        enabled = enabled,
        onClick = onClick,
        imageVector = imageVector,
        title = name,
        subtitle = boundNetworkKey.name,
        supportingText = null,
        actions = null
    )
}
