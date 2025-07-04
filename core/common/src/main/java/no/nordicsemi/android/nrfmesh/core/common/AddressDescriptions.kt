package no.nordicsemi.android.nrfmesh.core.common

import no.nordicsemi.kotlin.mesh.core.model.AllFriends
import no.nordicsemi.kotlin.mesh.core.model.AllNodes
import no.nordicsemi.kotlin.mesh.core.model.AllProxies
import no.nordicsemi.kotlin.mesh.core.model.AllRelays
import no.nordicsemi.kotlin.mesh.core.model.FixedGroupAddress

/**
 * Returns a human readable description of the [FixedGroupAddress].
 */
fun FixedGroupAddress.name() = when (this) {
    is AllProxies -> "All Proxies"
    is AllNodes -> "All Nodes"
    is AllRelays -> "All Relays"
    is AllFriends -> "All Friends"
}