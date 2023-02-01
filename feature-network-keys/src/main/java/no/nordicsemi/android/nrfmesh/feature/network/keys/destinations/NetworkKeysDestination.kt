package no.nordicsemi.android.nrfmesh.feature.network.keys.destinations

import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.network.keys.NetworkKeysRoute

val networkKeys = createDestination<Unit, Unit>("network_keys")

val networkKeysDestination = defineDestination(networkKeys) {
    NetworkKeysRoute(navigateToNetworkKey = {}) {}
}

val networkKeysDestinations = networkKeysDestination + networkKeyDestination