package no.nordicsemi.android.nrfmesh.feature.proxyfilter.destination

import no.nordicsemi.android.common.navigation.createSimpleDestination
import no.nordicsemi.android.common.navigation.defineDestination

val proxyFilter = createSimpleDestination("proxy_filter")

val proxyFilterDestination = defineDestination(proxyFilter) {}

val proxyFilterDestinations = listOf(proxyFilterDestination)