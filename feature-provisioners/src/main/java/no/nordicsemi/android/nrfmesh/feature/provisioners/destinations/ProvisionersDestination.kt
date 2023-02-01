@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.provisioners.destinations

import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import java.util.*

val provisioners = createDestination<Unit, Unit>("provisioners")

val provisionersDestination = defineDestination(provisioners) {}

val provisionersDestinations = provisionersDestination + provisionerDestination