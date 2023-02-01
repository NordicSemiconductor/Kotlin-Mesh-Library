@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.provisioners.destinations

import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import java.util.*

val provisioner = createDestination<UUID, Unit>("provisioner")

val provisionerDestination = defineDestination(provisioner) {}