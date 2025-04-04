package no.nordicsemi.android.nrfmesh.feature.provisioning.navigation

import android.os.Parcelable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.feature.provisioning.ProvisioningRoute
import no.nordicsemi.android.nrfmesh.feature.provisioning.ProvisioningViewModel

@Parcelize
@Serializable
data object ProvisioningRoute : Parcelable

fun NavController.navigateToProvisioning(navOptions: NavOptions) = navigate(
    route = ProvisioningRoute,
    navOptions = navOptions
)

fun NavGraphBuilder.provisioningGraph(appState: AppState, onBackPressed: () -> Unit) {
    composable<ProvisioningRoute> {
        val viewModel = hiltViewModel<ProvisioningViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ProvisioningRoute(
            uiState = uiState,
            beginProvisioning = viewModel::beginProvisioning,
            onNameChanged = viewModel::onNameChanged,
            onAddressChanged = viewModel::onAddressChanged,
            isValidAddress = viewModel::isValidAddress,
            onNetworkKeyClick = {

            },
            startProvisioning = viewModel::startProvisioning,
            authenticate = viewModel::authenticate,
            onProvisioningComplete = {
                viewModel.onProvisioningComplete()
                appState.navigateToNode(uuid = it)
            },
            onProvisioningFailed = {
                viewModel.onProvisioningFailed()
                onBackPressed()
            },
            disconnect = viewModel::disconnect
        )
    }
    netKeySelectorGraph(appState = appState)
}