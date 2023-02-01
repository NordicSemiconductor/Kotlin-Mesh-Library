@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.application.keys.destinations

import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.application.keys.ApplicationKeyRoute
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex

val applicationKey = createDestination<KeyIndex, Unit>("application_key")

val applicationKeyDestination = defineDestination(applicationKey) {
    ApplicationKeyRoute() {}
}