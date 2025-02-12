package no.nordicsemi.android.nrfmesh.feature.provisioning.navigation

import android.os.Parcelable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination.Companion.ARG
import no.nordicsemi.android.nrfmesh.feature.provisioning.NetKeySelectorRoute
import no.nordicsemi.android.nrfmesh.feature.provisioning.NetKeySelectorViewModel
import no.nordicsemi.android.nrfmesh.feature.provisioning.ProvisioningViewModel

@Parcelize
@Serializable
data object NetKeySelectorRoute : Parcelable

internal fun NavGraphBuilder.netKeySelectorGraph(appState: AppState) {
    composable<NetKeySelectorRoute> {
        val viewModel = hiltViewModel<NetKeySelectorViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        // The previousBackStackEntry is used to set the result back to the previous screen.
        // https://stackoverflow.com/questions/76892268/jetpack-compose-sending-result-back-with-savedstatehandle-does-not-work-with-sav/76901998#76901998
        val previousBackStackEntry = remember(it) {
            appState.previousBackStackEntry!!
        }
        val previousViewModel = hiltViewModel<ProvisioningViewModel>(previousBackStackEntry)
        NetKeySelectorRoute(
            uiState = uiState,
            onKeySelected = { keyIndex ->
                viewModel.onKeySelected(keyIndex)
                previousViewModel.savedStateHandle[ARG] = keyIndex.toInt().toString()
            }
        )
    }
}