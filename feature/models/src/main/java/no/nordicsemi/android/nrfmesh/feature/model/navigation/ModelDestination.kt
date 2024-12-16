package no.nordicsemi.android.nrfmesh.feature.model.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination.Companion.ARG
import no.nordicsemi.android.nrfmesh.feature.bind.appkeys.navigation.BoundAppKeysDestination
import no.nordicsemi.android.nrfmesh.feature.bind.appkeys.navigation.BoundAppKeysDestination.bindAppKeysGraph
import no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.navigation.configApplicationKeysGraph
import no.nordicsemi.android.nrfmesh.feature.groups.navigation.GroupsDestination
import no.nordicsemi.android.nrfmesh.feature.model.ModelRoute
import no.nordicsemi.android.nrfmesh.feature.model.ModelViewModel
import no.nordicsemi.kotlin.mesh.core.model.Model

object ModelDestination : MeshNavigationDestination {
    const val MODEL_ID = "MODEL_ID"
    override val route: String = "model_route/{$ARG}/{$MODEL_ID}"
    override val destination: String = "model_destination"

    /**
     * Creates destination route for a given Model
     *
     * @param model Model to navigate to.
     * @return The route string.
     */
    @Throws(IllegalStateException::class)
    fun createNavigationRoute(model: Model): String {
        val address = model.parentElement?.unicastAddress
            ?: throw IllegalStateException("Parent element address is null")
        return "model_route/${address.toHexString()}/${model.modelId.toHex()}"
    }

    fun NavGraphBuilder.modelGraph(
        appState: AppState,
        onNavigateToDestination: (MeshNavigationDestination, String) -> Unit,
        onBackPressed: () -> Unit
    ) {
        composable(route = ModelDestination.route) {
            val viewModel = hiltViewModel<ModelViewModel>()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            ModelRoute(
                appState = appState,
                uiState = uiState,
                send = viewModel::send,
                navigateToBoundAppKeys = {
                    onNavigateToDestination(
                        BoundAppKeysDestination,
                        BoundAppKeysDestination.createNavigationRoute(it)
                    )
                },
                requestNodeIdentityStates = viewModel::requestNodeIdentityStates,
                resetMessageState = viewModel::resetMessageState,
                onAddGroupClicked = {
                    onNavigateToDestination(
                        appState.topLevelDestinations.first {
                            it.route == GroupsDestination.route
                        },
                        GroupsDestination.route
                    )
                },
                onBackPressed = onBackPressed
            )
        }
        bindAppKeysGraph(
            appState = appState,
            onNavigateToDestination = onNavigateToDestination,
            onBackPressed = onBackPressed
        )
        configApplicationKeysGraph(
            appState = appState,
            onNavigateToDestination = onNavigateToDestination,
            onBackPressed = onBackPressed
        )
    }
}