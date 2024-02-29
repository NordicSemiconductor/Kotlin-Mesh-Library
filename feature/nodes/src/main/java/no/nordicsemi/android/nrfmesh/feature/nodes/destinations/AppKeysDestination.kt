package no.nordicsemi.android.nrfmesh.feature.nodes.destinations

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.common.navigation.createSimpleDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.nodes.NodesScreenUiState
import no.nordicsemi.android.nrfmesh.feature.nodes.NodesViewModel

val appKeys = createSimpleDestination("app_keys")

val appKeysDestination = defineDestination(appKeys) {
    val viewModel: NodesViewModel = hiltViewModel()
    val uiState: NodesScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()

}

