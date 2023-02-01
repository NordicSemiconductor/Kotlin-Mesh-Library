@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.network.keys.destinations

import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex

val networkKey = createDestination<KeyIndex, Unit>("network_key")

val networkKeyDestination = defineDestination(networkKey) {}