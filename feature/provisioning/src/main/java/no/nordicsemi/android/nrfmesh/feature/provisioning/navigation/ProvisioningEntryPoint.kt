package no.nordicsemi.android.nrfmesh.feature.provisioning.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination.Companion.ARG
import no.nordicsemi.android.nrfmesh.core.navigation.Navigator
import no.nordicsemi.android.nrfmesh.feature.provisioning.NetKeySelectorRoute
import no.nordicsemi.android.nrfmesh.feature.provisioning.NetKeySelectorViewModel
import no.nordicsemi.android.nrfmesh.feature.provisioning.ProvisioningRoute
import no.nordicsemi.android.nrfmesh.feature.provisioning.ProvisioningViewModel
import no.nordicsemi.android.nrfmesh.core.navigation.NodeKey
import no.nordicsemi.android.nrfmesh.core.navigation.NodesKey
import kotlin.uuid.ExperimentalUuidApi

@Serializable
data object ProvisioningKey : NavKey

@OptIn(ExperimentalUuidApi::class)
fun NavGraphBuilder.provisioningGraph(appState: AppState, onBackPressed: () -> Unit) {
    composable<ProvisioningKey> {
        val viewModel = hiltViewModel<ProvisioningViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ProvisioningRoute(
            uiState = uiState,
            beginProvisioning = viewModel::beginProvisioning,
            onNameChanged = viewModel::onNameChanged,
            onAddressChanged = viewModel::onAddressChanged,
            isValidAddress = viewModel::isValidAddress,
            onNetworkKeyClicked = viewModel::onNetworkKeyClicked,
            onAuthenticationMethodSelected = viewModel::onAuthenticationMethodSelected,
            authenticate = viewModel::authenticate,
            onProvisioningComplete = {
                viewModel.onProvisioningComplete()
            },
            onProvisioningFailed = {
                viewModel.onProvisioningFailed()
                onBackPressed()
            },
            disconnect = viewModel::disconnect
        )
    }
    // netKeySelectorGraph(appState = appState)
}

@OptIn(ExperimentalUuidApi::class)
fun EntryProviderScope<NavKey>.provisioningEntry(appState: AppState, navigator: Navigator) {
    entry<ProvisioningKey> {
        val viewModel = hiltViewModel<ProvisioningViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ProvisioningRoute(
            uiState = uiState,
            beginProvisioning = viewModel::beginProvisioning,
            onNameChanged = viewModel::onNameChanged,
            onAddressChanged = viewModel::onAddressChanged,
            isValidAddress = viewModel::isValidAddress,
            onNetworkKeyClicked = viewModel::onNetworkKeyClicked,
            onAuthenticationMethodSelected = viewModel::onAuthenticationMethodSelected,
            authenticate = viewModel::authenticate,
            onProvisioningComplete = {
                viewModel.onProvisioningComplete()
                navigator.navigate(key = NodesKey)
                navigator.navigate(key = NodeKey(nodeUuid = it.toString()))
            },
            onProvisioningFailed = {
                viewModel.onProvisioningFailed()
                navigator.goBack()
            },
            disconnect = viewModel::disconnect
        )
    }
    //TODO Need to check how to return a result
    entry<NetKeySelectorKey> {
        entry<NetKeySelectorKey> {
            val viewModel = hiltViewModel<NetKeySelectorViewModel>()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            // The previousBackStackEntry is used to set the result back to the previous screen.
            // https://stackoverflow.com/questions/76892268/jetpack-compose-sending-result-back-with-savedstatehandle-does-not-work-with-sav/76901998#76901998
            // val previousBackStackEntry = remember(it) {
            //     appState.previousBackStackEntry!!
            // }
            // val previousViewModel = hiltViewModel<ProvisioningViewModel>(previousBackStackEntry)
            // NetKeySelectorRoute(
            //     uiState = uiState,
            //     onKeySelected = { keyIndex ->
            //         viewModel.onKeySelected(keyIndex)
            //         previousViewModel.savedStateHandle[ARG] = keyIndex.toInt().toString()
            //     }
            // )
        }
    }
}