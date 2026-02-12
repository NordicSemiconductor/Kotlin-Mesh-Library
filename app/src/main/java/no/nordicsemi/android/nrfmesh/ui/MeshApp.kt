package no.nordicsemi.android.nrfmesh.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.nrfmesh.core.navigation.MESH_TOP_LEVEL_NAV_ITEMS
import no.nordicsemi.android.nrfmesh.core.navigation.NodesKey
import no.nordicsemi.android.nrfmesh.core.navigation.rememberNavigationState
import no.nordicsemi.android.nrfmesh.navigation.rememberMeshAppState
import no.nordicsemi.android.nrfmesh.ui.network.NetworkScreen
import no.nordicsemi.android.nrfmesh.viewmodel.NetworkViewModel

@Composable
fun MeshApp() {
    val snackbarHostState = remember { SnackbarHostState() }
    val navigationState = rememberNavigationState(
        startKey = NodesKey,
        topLevelKeys = MESH_TOP_LEVEL_NAV_ITEMS.keys
    )
    val appState = rememberMeshAppState(
        snackbarHostState = snackbarHostState, navigationState = navigationState
    )
    val viewModel = hiltViewModel<NetworkViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NetworkScreen(
        appState = appState,
        uiState = uiState,
        shouldSelectProvisioner = uiState.shouldSelectProvisioner,
        onProvisionerSelected = viewModel::onProvisionerSelected,
        importNetwork = viewModel::importNetwork,
        resetNetwork = viewModel::resetNetwork
    )
}