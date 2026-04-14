package no.nordicsemi.android.nrfmesh.feature.model.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.GroupsKey
import no.nordicsemi.android.nrfmesh.core.navigation.Navigator
import no.nordicsemi.android.nrfmesh.core.navigation.NodeListDetailSceneKey
import no.nordicsemi.android.nrfmesh.feature.model.ModelScreen
import no.nordicsemi.android.nrfmesh.feature.model.ModelViewModel
import no.nordicsemi.kotlin.mesh.core.model.Address
import kotlin.uuid.ExperimentalUuidApi

@Serializable
data class ModelKey(val address: Address, val modelId: UInt) : NavKey

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalUuidApi::class)
fun EntryProviderScope<NavKey>.modelEntry(appState: AppState, navigator: Navigator) {
    entry<ModelKey>(
        metadata = ListDetailSceneStrategy.extraPane(
            sceneKey = NodeListDetailSceneKey
        )
    ) { key ->
        val viewModel = hiltViewModel<ModelViewModel, ModelViewModel.Factory> {
            it.create(key.address.toInt(), key.modelId.toInt())
        }
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ModelScreen(
            snackbarHostState = appState.snackbarHostState,
            modelState = uiState.modelState,
            messageState = uiState.messageState,
            nodeIdentityStates = uiState.nodeIdentityStates,
            requestNodeIdentityStates = viewModel::requestNodeIdentityStates,
            onAddGroupClicked = { navigator.navigate(GroupsKey) },
            resetMessageState = viewModel::resetMessageState,
            navigateToGroups = { navigator.navigate(key = GroupsKey) },
            navigateToConfigApplicationKeys = {},
            send = viewModel::send,
            sendApplicationMessage = viewModel::sendApplicationMessage
        )
    }
}