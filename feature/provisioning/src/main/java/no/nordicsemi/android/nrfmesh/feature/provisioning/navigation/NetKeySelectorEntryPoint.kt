package no.nordicsemi.android.nrfmesh.feature.provisioning.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object NetKeySelectorKey : NavKey

// TODO Implement NetKeySelectorEntryPoint

// @OptIn(ExperimentalUuidApi::class)
// fun EntryProviderScope<NavKey>.netKeySelectorEntry(appState: AppState, navigator: Navigator) {
//     entry<NetKeySelectorKey> {
//         val viewModel = hiltViewModel<NetKeySelectorViewModel>()
//         val uiState by viewModel.uiState.collectAsStateWithLifecycle()
//         // The previousBackStackEntry is used to set the result back to the previous screen.
//         // https://stackoverflow.com/questions/76892268/jetpack-compose-sending-result-back-with-savedstatehandle-does-not-work-with-sav/76901998#76901998
//         val previousBackStackEntry = remember(it) {
//             appState.previousBackStackEntry!!
//         }
//         val previousViewModel = hiltViewModel<ProvisioningViewModel>(previousBackStackEntry)
//         NetKeySelectorRoute(
//             uiState = uiState,
//             onKeySelected = { keyIndex ->
//                 viewModel.onKeySelected(keyIndex)
//                 previousViewModel.savedStateHandle[ARG] = keyIndex.toInt().toString()
//             }
//         )
//     }
// }