package no.nordicsemi.android.nrfmesh.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.nrfmesh.ui.network.NetworkRoute
import no.nordicsemi.android.nrfmesh.viewmodel.NetworkViewModel

@Composable
fun MeshApp() {
    val viewModel = hiltViewModel<NetworkViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NetworkRoute(
        provisioners = uiState.provisioners,
        shouldSelectProvisioner = uiState.shouldSelectProvisioner,
        onProvisionerSelected = viewModel::onProvisionerSelected,
        importNetwork = viewModel::importNetwork,
        resetNetwork = viewModel::resetNetwork,
        onAddGroupClicked = viewModel::onAddGroupClicked,
        nextAvailableGroupAddress = viewModel::nextAvailableGroupAddress
    )
}