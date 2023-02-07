package no.nordicsemi.android.nrfmesh.feature.groups.destinations

import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.groups.Groups
import no.nordicsemi.kotlin.mesh.core.model.GroupAddress


val group = createDestination<GroupAddress, Unit>("group")

val groupDestination = defineDestination(group) {
    Groups()
}