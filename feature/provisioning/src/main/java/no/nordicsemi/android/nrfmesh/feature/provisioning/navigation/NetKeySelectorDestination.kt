package no.nordicsemi.android.nrfmesh.feature.provisioning.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination.Companion.ARG
import no.nordicsemi.android.nrfmesh.feature.provisioning.NetKeySelectorRoute
import no.nordicsemi.android.nrfmesh.feature.provisioning.NetKeySelectorViewModel
import no.nordicsemi.android.nrfmesh.feature.provisioning.ProvisioningViewModel

object NetKeySelectorDestination : MeshNavigationDestination {
    override val route: String = "net_key_selector_route"
    override val destination: String = "net_key_selector_destination"
}

internal fun NavGraphBuilder.netKeySelectorGraph(
    appState: AppState,
    onBackPressed: () -> Unit
) {
    composable(route = NetKeySelectorDestination.route) {
        val viewModel = hiltViewModel<NetKeySelectorViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        // The previousBackStackEntry is used to set the result back to the previous screen.
        // https://stackoverflow.com/questions/76892268/jetpack-compose-sending-result-back-with-savedstatehandle-does-not-work-with-sav/76901998#76901998
        val previousBackStackEntry = remember(it) {
            appState.previousBackStackEntry!!
        }
        val previousViewModel = hiltViewModel<ProvisioningViewModel>(previousBackStackEntry)
        NetKeySelectorRoute(
            appState = appState,
            uiState = uiState,
            onKeySelected = { keyIndex ->
                viewModel.onKeySelected(keyIndex)
                previousViewModel.savedStateHandle[ARG] = keyIndex.toInt().toString()
            },
            onBackPressed = onBackPressed
        )
    }
}