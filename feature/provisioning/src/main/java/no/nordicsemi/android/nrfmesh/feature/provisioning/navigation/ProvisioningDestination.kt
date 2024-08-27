package no.nordicsemi.android.nrfmesh.feature.provisioning.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.provisioning.ProvisioningRoute
import no.nordicsemi.android.nrfmesh.feature.provisioning.ProvisioningViewModel

object ProvisioningDestination : MeshNavigationDestination {
    override val route: String = "provisioning_route"
    override val destination: String = "provisioning_destination"
}

fun NavGraphBuilder.provisioningGraph(
    appState: AppState,
    onNavigateToDestination: (MeshNavigationDestination, String) -> Unit,
    onBackPressed: () -> Unit
) {
    composable(route = ProvisioningDestination.route) {
        val viewModel = hiltViewModel<ProvisioningViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ProvisioningRoute(
            appState = appState,
            uiState = uiState,
            beginProvisioning = viewModel::beginProvisioning,
            onNameChanged = viewModel::onNameChanged,
            onAddressChanged = viewModel::onAddressChanged,
            isValidAddress = viewModel::isValidAddress,
            onNetworkKeyClick = {
                onNavigateToDestination(
                    NetKeySelectorDestination,
                    NetKeySelectorDestination.route
                )
            },
            startProvisioning = viewModel::startProvisioning,
            authenticate = viewModel::authenticate,
            onProvisioningComplete = {
                viewModel.onProvisioningComplete()
                onBackPressed()
            },
            onProvisioningFailed = {
                viewModel.onProvisioningFailed()
                onBackPressed()
            },
            disconnect = viewModel::disconnect
        )
    }
    netKeySelectorGraph(
        appState = appState,
        onBackPressed = onBackPressed
    )
}