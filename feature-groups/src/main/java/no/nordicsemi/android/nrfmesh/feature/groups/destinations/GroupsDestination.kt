package no.nordicsemi.android.nrfmesh.feature.groups.destinations

import no.nordicsemi.android.common.navigation.createSimpleDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.groups.Groups

val groups = createSimpleDestination("groups")

val groupsDestination = defineDestination(groups) {
    Groups()
}

val groupsDestinations = groupsDestination + groupDestination