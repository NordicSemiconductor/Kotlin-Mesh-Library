@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.provisioners.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.Navigator
import no.nordicsemi.android.nrfmesh.feature.provisioners.ProvisionersScreen
import no.nordicsemi.android.nrfmesh.feature.provisioners.ProvisionersViewModel
import no.nordicsemi.android.nrfmesh.feature.provisioners.provisioner.navigation.ProvisionerContentKey
import no.nordicsemi.android.nrfmesh.feature.provisioners.provisioner.navigation.provisionerEntry
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data object ProvisionersContentKey : NavKey

@OptIn(ExperimentalUuidApi::class)
@Composable
fun ProvisionersScreenRoute(
    snackbarHostState: SnackbarHostState,
    highlightSelectedItem: Boolean,
    onProvisionerClicked: (Uuid) -> Unit,
    navigateToProvisioner: (Uuid) -> Unit,
    navigateUp: () -> Unit,
) {
    val viewModel = hiltViewModel<ProvisionersViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProvisionersScreen(
        snackbarHostState = snackbarHostState,
        highlightSelectedItem = highlightSelectedItem,
        selectedProvisionerUuid = uiState.selectedProvisionerUuid,
        provisioners = uiState.provisioners,
        onAddProvisionerClicked = viewModel::addProvisioner,
        onProvisionerClicked = {
            viewModel.selectProvisioner(uuid = it)
            onProvisionerClicked(it)
        },
        navigateToProvisioner = {
            viewModel.selectProvisioner(uuid = it)
            navigateToProvisioner(it)
        },
        onSwiped = {
            viewModel.onSwiped(provisioner = it)
            if (uiState.selectedProvisionerUuid == it.uuid) {
                navigateUp()
            }
        },
        onUndoClicked = viewModel::onUndoSwipe,
        remove = viewModel::remove
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalUuidApi::class)
fun EntryProviderScope<NavKey>.provisionersEntry(appState: AppState, navigator: Navigator) {
    entry<ProvisionersContentKey>(
        metadata = ListDetailSceneStrategy.detailPane()
    ) {
        val viewModel = hiltViewModel<ProvisionersViewModel, ProvisionersViewModel.Factory>(
            key = "ProvisionersViewModel"
        ) { factory ->
            factory.create()
        }
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ProvisionersScreen(
            snackbarHostState = appState.snackbarHostState,
            highlightSelectedItem = false,
            selectedProvisionerUuid = uiState.selectedProvisionerUuid,
            provisioners = uiState.provisioners,
            onAddProvisionerClicked = viewModel::addProvisioner,
            onProvisionerClicked = {
                viewModel.selectProvisioner(uuid = it)
                navigator.navigate(key = ProvisionerContentKey(uuid = it))
            },
            navigateToProvisioner = {
                viewModel.selectProvisioner(uuid = it)
                navigator.navigate(key = ProvisionerContentKey(uuid = it))
            },
            onSwiped = {
                viewModel.onSwiped(provisioner = it)
                if (uiState.selectedProvisionerUuid == it.uuid) {
                    navigator.goBack()
                }
            },
            onUndoClicked = viewModel::onUndoSwipe,
            remove = viewModel::remove
        )
    }
    provisionerEntry(appState = appState, navigator = navigator)
}