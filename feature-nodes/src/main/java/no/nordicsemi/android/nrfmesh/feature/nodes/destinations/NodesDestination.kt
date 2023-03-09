package no.nordicsemi.android.nrfmesh.feature.nodes.destinations

import no.nordicsemi.android.common.navigation.createSimpleDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.nodes.Nodes

val nodes = createSimpleDestination("nodes")

val nodesDestination = defineDestination(nodes) {
    Nodes()
}

val nodesDestinations = nodesDestination + nodeDestination