@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.application.keys.destinations

import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.application.keys.ApplicationKeysRoute
import no.nordicsemi.android.nrfmesh.feature.application.keys.ApplicationKeysViewModel

val applicationKeys = createDestination<Unit, Unit>("application_keys")

val applicationKeysDestination = defineDestination(applicationKeys) {
    val viewModel: ApplicationKeysViewModel = hiltViewModel()
    ApplicationKeysRoute(viewModel = viewModel) { keyIndex ->
        viewModel.navigate(applicationKey, keyIndex.toInt())
    }
}

val applicationKeysDestinations = applicationKeysDestination + applicationKeyDestination