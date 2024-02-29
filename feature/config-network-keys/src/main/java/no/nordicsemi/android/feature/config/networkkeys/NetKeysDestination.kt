package no.nordicsemi.android.feature.config.networkkeys

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.network.keys.destinations.networkKeysDestinations
import java.util.UUID

val configNetKeys = createDestination<UUID, Unit>("net_keys")

private val configNetKeyDestination = defineDestination(configNetKeys) {
    val viewModel: NetKeysViewModel = hiltViewModel()
    val uiState: NetKeysScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    NetKeysRoute(
        viewModel = viewModel,
        navigateToNetworkKeys = viewModel::navigateToNetworkKeys
    )
}

val configNetKeyDestinations = configNetKeyDestination + networkKeysDestinations