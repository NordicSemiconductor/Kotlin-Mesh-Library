package no.nordicsemi.android.nrfmesh.core.common

import no.nordicsemi.kotlin.mesh.core.model.Node

/**
 * Returns a list of network keys that are not known to the node.
 */
fun Node.unknownNetworkKeys() = network?.networkKeys
    .orEmpty()
    .filter { !knows(it) }


/**
 * Returns a list of network keys that are not known to the node.
 */
fun Node.unknownApplicationKeys() = network?.applicationKeys
    .orEmpty()
    .filter { !knows(it) }