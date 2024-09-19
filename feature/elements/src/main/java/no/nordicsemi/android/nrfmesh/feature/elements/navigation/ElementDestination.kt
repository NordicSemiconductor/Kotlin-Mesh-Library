package no.nordicsemi.android.nrfmesh.feature.elements.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination.Companion.ARG
import no.nordicsemi.android.nrfmesh.feature.configurationserver.navigation.ConfigurationServerDestination
import no.nordicsemi.android.nrfmesh.feature.configurationserver.navigation.configurationServerGraph
import no.nordicsemi.android.nrfmesh.feature.elements.ElementRoute
import no.nordicsemi.android.nrfmesh.feature.elements.ElementViewModel
import no.nordicsemi.kotlin.mesh.core.model.Address
import no.nordicsemi.kotlin.mesh.core.model.Model

object ElementDestination : MeshNavigationDestination {
    override val route: String = "element_route/{$ARG}"
    override val destination: String = "element_destination"

    /**
     * Creates destination route for a selected element.
     *
     * @param address Address of the element
     */
    @OptIn(ExperimentalStdlibApi::class)
    fun createNavigationRoute(address: Address): String =
        "element_route/${address.toHexString()}"
}

fun NavGraphBuilder.elementGraph(
    appState: AppState,
    onNavigateToDestination: (MeshNavigationDestination, String) -> Unit,
    onBackPressed: () -> Unit
) {
    composable(route = ElementDestination.route) {
        val viewModel = hiltViewModel<ElementViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ElementRoute(
            appState = appState,
            uiState = uiState,
            onNameChanged = viewModel::onNameChanged,
            navigateToModel = { navigate(it, onNavigateToDestination) },
            onBackPressed = onBackPressed
        )
    }
    configurationServerGraph(
        appState = appState,
        onNavigateToDestination = onNavigateToDestination,
        onBackPressed = onBackPressed
    )
}

private fun navigate(
    model: Model,
    onNavigateToDestination: (MeshNavigationDestination, String) -> Unit
) {
    val address = model.parentElement?.unicastAddress?.address
        ?: throw IllegalArgumentException("Parent element address is null")
    when {
        model.isConfigurationServer -> onNavigateToDestination(
            ConfigurationServerDestination,
            ConfigurationServerDestination.createNavigationRoute(address = address)
        )
    }
}