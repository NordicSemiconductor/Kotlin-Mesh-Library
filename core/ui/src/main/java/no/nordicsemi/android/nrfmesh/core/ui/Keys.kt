package no.nordicsemi.android.nrfmesh.core.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey

@Composable
fun NetworkKey.Row(
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.outlinedCardColors(),
    onClick: (() -> Unit)? = null,
    imageVector: ImageVector = Icons.Outlined.VpnKey,
) = when (onClick != null) {
    true -> ElevatedCardItem(
        modifier = modifier,
        colors = colors,
        onClick = onClick,
        imageVector = imageVector,
        title = name,
    )

    else -> ElevatedCardItem(
        modifier = modifier,
        colors = colors,
        imageVector = imageVector,
        title = name,
    )
}

@Composable
fun ApplicationKey.Row(
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.outlinedCardColors(),
    onClick: (() -> Unit)? = null,
    imageVector: ImageVector = Icons.Outlined.VpnKey,
) = when (onClick != null) {
    true -> ElevatedCardItem(
        modifier = modifier,
        colors = colors,
        onClick = onClick,
        imageVector = imageVector,
        title = name,
        subtitle = "Bound to: ${boundNetworkKey.name}",
    )

    else -> ElevatedCardItem(
        modifier = modifier,
        colors = colors,
        imageVector = imageVector,
        title = name,
        subtitle = "Bound to: ${boundNetworkKey.name}"
    )
}
