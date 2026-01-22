package no.nordicsemi.android.nrfmesh.feature.provisioners.provisioner.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.Navigator
import no.nordicsemi.android.nrfmesh.feature.provisioners.provisioner.ProvisionerScreen
import no.nordicsemi.android.nrfmesh.feature.provisioners.provisioner.ProvisionerViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class ProvisionerContentKey(val uuid: String) : NavKey {
    constructor(uuid: Uuid) : this(uuid = uuid.toString())
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalUuidApi::class)
fun EntryProviderScope<NavKey>.provisionerEntry(appState: AppState, navigator: Navigator) {
    entry<ProvisionerContentKey>(
        metadata = ListDetailSceneStrategy.extraPane()
    ) { key ->
        val uuid = key.uuid
        val viewModel = hiltViewModel<ProvisionerViewModel, ProvisionerViewModel.Factory>(
            key = "ProvisionerViewModel:${key.uuid}"
        ) { factory ->
            factory.create(uuid = Uuid.parse(uuidString = uuid))
        }
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ProvisionerScreen(
            uiState = uiState,
            snackbarHostState = appState.snackbarHostState,
            moveProvisioner = viewModel::moveProvisioner,
            save = viewModel::save
        )
    }
}