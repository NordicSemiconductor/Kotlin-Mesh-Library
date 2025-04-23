package no.nordicsemi.android.nrfmesh.core.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GroupWork
import androidx.compose.material.icons.outlined.Lan
import androidx.compose.material.icons.outlined.ViewInAr
import androidx.compose.runtime.Composable
import no.nordicsemi.kotlin.mesh.core.model.FixedGroupAddress
import no.nordicsemi.kotlin.mesh.core.model.GroupAddress
import no.nordicsemi.kotlin.mesh.core.model.MeshAddress
import no.nordicsemi.kotlin.mesh.core.model.ProxyFilterAddress
import no.nordicsemi.kotlin.mesh.core.model.UnassignedAddress
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.VirtualAddress

@Composable
fun MeshAddress.toIcon() = when (this) {
    is UnassignedAddress -> Icons.Outlined.Lan
    is UnicastAddress -> Icons.Outlined.Lan
    is VirtualAddress -> Icons.Outlined.ViewInAr
    is GroupAddress -> Icons.Outlined.GroupWork
    is FixedGroupAddress -> Icons.Outlined.GroupWork
}
@Composable
fun ProxyFilterAddress.toIcon() = when (this) {
    is UnicastAddress -> Icons.Outlined.Lan
    is VirtualAddress -> Icons.Outlined.ViewInAr
    is GroupAddress -> Icons.Outlined.GroupWork
    is FixedGroupAddress -> Icons.Outlined.GroupWork
}