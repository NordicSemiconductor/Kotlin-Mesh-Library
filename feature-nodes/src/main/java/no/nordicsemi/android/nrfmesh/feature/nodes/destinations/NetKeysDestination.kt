package no.nordicsemi.android.nrfmesh.feature.nodes.destinations

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.nodes.netkeys.NetKeysRoute
import no.nordicsemi.android.nrfmesh.feature.nodes.netkeys.NetKeysScreenUiState
import no.nordicsemi.android.nrfmesh.feature.nodes.netkeys.NetKeysViewModel
import java.util.UUID

val netKeys = createDestination<UUID, Unit>("net_keys")

val netKeysDestination = defineDestination(netKeys) {
    val viewModel: NetKeysViewModel = hiltViewModel()
    val uiState: NetKeysScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    NetKeysRoute(
        viewModel = viewModel,
        navigateToNetworkKeys = viewModel::navigateToNetworkKeys
    )
}

