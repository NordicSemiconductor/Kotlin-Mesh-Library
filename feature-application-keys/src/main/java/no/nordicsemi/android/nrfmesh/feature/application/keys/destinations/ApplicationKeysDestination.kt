@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.application.keys.destinations

import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.application.keys.ApplicationKeysRoute

val applicationKeys = createDestination<Unit, Unit>("application_keys")

val applicationKeysDestination = defineDestination(applicationKeys) {
    ApplicationKeysRoute(navigateToApplicationKey = {}) {}
}

val applicationKeysDestinations = applicationKeysDestination + applicationKeyDestination