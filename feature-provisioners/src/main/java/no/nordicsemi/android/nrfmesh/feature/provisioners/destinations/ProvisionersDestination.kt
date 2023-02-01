@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.provisioners.destinations

import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.provisioners.ProvisionersRoute
import java.util.*

val provisioners = createDestination<Unit, Unit>("provisioners")

val provisionersDestination = defineDestination(provisioners) {
    ProvisionersRoute(navigateToProvisioner = {

    }) {

    }
}

val provisionersDestinations = provisionersDestination + provisionerDestination