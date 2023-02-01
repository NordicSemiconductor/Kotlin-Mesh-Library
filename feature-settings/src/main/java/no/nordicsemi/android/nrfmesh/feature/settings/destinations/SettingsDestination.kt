package no.nordicsemi.android.nrfmesh.feature.settings.destinations

import no.nordicsemi.android.common.navigation.createSimpleDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.settings.SettingsRoute

val settings = createSimpleDestination("settings")

val settingsDestination = defineDestination(settings) {
    SettingsRoute(
        navigateToProvisioners = { /*TODO*/ },
        navigateToNetworkKeys = { /*TODO*/ },
        navigateToApplicationKeys = { /*TODO*/ },
        navigateToScenes = { /*TODO*/ }) {

    }
}

val settingsDestinations = listOf(settingsDestination)