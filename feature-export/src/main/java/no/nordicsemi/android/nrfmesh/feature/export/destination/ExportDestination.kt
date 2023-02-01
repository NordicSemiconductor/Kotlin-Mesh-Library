package no.nordicsemi.android.nrfmesh.feature.export.destination

import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import java.util.*

val export = createDestination<UUID, Unit>("export")

val exportDestination = defineDestination(export) {}

val exportDestinations = exportDestination