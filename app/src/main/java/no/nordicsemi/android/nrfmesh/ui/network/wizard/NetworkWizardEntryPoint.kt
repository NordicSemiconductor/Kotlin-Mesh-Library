package no.nordicsemi.android.nrfmesh.ui.network.wizard

import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
object NetworkWizardKey : NavKey

fun EntryProviderScope<NavKey>.networkWizardEntry(
    navigateToNetwork: () -> Unit
) = entry<NetworkWizardKey> {
    val viewModel = hiltViewModel<NetworkWizardViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NetworkWizardScreen(
        configurations = uiState.configurations,
        configuration = uiState.configuration,
        onConfigurationSelected = viewModel::onConfigurationSelected,
        add = viewModel::increment,
        remove = viewModel::decrement,
        onContinuePressed = {
            viewModel.onContinuePressed()
            navigateToNetwork()
        },
        importNetwork = { uri, contentResolver ->
            viewModel.importNetwork(uri, contentResolver)
            navigateToNetwork()
        }
    )
}