package no.nordicsemi.android.nrfmesh.feature.network.keys.destinations

import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex

val networkKeys = createDestination<KeyIndex, Unit>("network_keys")

val networkKeysDestination = defineDestination(networkKeys) {}

val networkKeysDestinations = networkKeysDestination + networkKeyDestination