package no.nordicsemi.android.nrfmesh.core.common

import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.NodeIdentityState

data class NodeIdentityStatus(
    val networkKey: NetworkKey,
    val nodeIdentityState: NodeIdentityState?
)
