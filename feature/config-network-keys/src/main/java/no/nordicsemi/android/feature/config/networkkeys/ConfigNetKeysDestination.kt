package no.nordicsemi.android.feature.config.networkkeys

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.network.keys.destinations.networkKeysDestinations
import no.nordicsemi.android.nrfmesh.feature.settings.destinations.settingsDestinations
import java.util.UUID

val configNetKeys = createDestination<UUID, Unit>("net_keys")

private val configNetKeyDestination = defineDestination(configNetKeys) {
    val viewModel: ConfigNetKeysViewModel = hiltViewModel()
    val uiState: NetKeysScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    ConfigNetKeysRoute(
        uiState = uiState,
        navigateToNetworkKeys = viewModel::navigateToNetworkKeys,
        onAddKeyClicked = viewModel::addNetworkKey,
        onSwiped = viewModel::onSwiped,
        resetMessageState = viewModel::resetMessageState,
        onBackClick = viewModel::navigateUp
    )
}

val configNetKeyDestinations = configNetKeyDestination + settingsDestinations + networkKeysDestinations