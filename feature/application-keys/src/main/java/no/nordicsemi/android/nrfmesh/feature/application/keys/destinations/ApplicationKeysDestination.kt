@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.application.keys.destinations

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.application.keys.ApplicationKeysRoute
import no.nordicsemi.android.nrfmesh.feature.application.keys.ApplicationKeysScreenUiState
import no.nordicsemi.android.nrfmesh.feature.application.keys.ApplicationKeysViewModel

val applicationKeys = createDestination<Unit, Unit>("application_keys")

private val applicationKeysDestination = defineDestination(applicationKeys) {
    val viewModel: ApplicationKeysViewModel = hiltViewModel()
    val uiState: ApplicationKeysScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    ApplicationKeysRoute(
        uiState = uiState,
        navigateToApplicationKey = viewModel::navigateToApplicationKey,
        onAddKeyClicked = viewModel::addApplicationKey,
        onSwiped = viewModel::onSwiped,
        onUndoClicked = viewModel::onUndoSwipe,
        remove = viewModel::remove
    )
}

val applicationKeysDestinations = applicationKeysDestination + applicationKeyDestination