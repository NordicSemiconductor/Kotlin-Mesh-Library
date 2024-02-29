package no.nordicsemi.android.nrfmesh.feature.export.destination

import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.export.ExportRoute
import java.util.UUID

val export = createDestination<UUID, Unit>("export")

val exportDestination = defineDestination(export) {
    ExportRoute()
}

val exportDestinations = exportDestination