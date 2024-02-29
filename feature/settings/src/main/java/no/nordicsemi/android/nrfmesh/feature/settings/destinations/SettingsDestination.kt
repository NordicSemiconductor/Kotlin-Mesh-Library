package no.nordicsemi.android.nrfmesh.feature.settings.destinations

import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.common.navigation.createSimpleDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.application.keys.destinations.applicationKeys
import no.nordicsemi.android.nrfmesh.feature.application.keys.destinations.applicationKeysDestinations
import no.nordicsemi.android.nrfmesh.feature.network.keys.destinations.networkKeys
import no.nordicsemi.android.nrfmesh.feature.network.keys.destinations.networkKeysDestinations
import no.nordicsemi.android.nrfmesh.feature.provisioners.destinations.provisioners
import no.nordicsemi.android.nrfmesh.feature.provisioners.destinations.provisionersDestinations
import no.nordicsemi.android.nrfmesh.feature.scenes.destination.scenes
import no.nordicsemi.android.nrfmesh.feature.scenes.destination.scenesDestinations
import no.nordicsemi.android.nrfmesh.feature.settings.SettingsRoute
import no.nordicsemi.android.nrfmesh.feature.settings.SettingsViewModel

val settings = createSimpleDestination("settings")

val settingsDestination = defineDestination(settings) {
    val viewModel: SettingsViewModel = hiltViewModel()

    SettingsRoute(
        viewModel = viewModel,
        navigateToProvisioners = { viewModel.navigateTo(provisioners) },
        navigateToNetworkKeys = { viewModel.navigateTo(networkKeys) },
        navigateToApplicationKeys = { viewModel.navigateTo(applicationKeys) },
        navigateToScenes = { viewModel.navigateTo(scenes) },
        navigateToExportNetwork = {}
    )
}

val settingsDestinations = settingsDestination +
        provisionersDestinations +
        networkKeysDestinations +
        applicationKeysDestinations +
        scenesDestinations