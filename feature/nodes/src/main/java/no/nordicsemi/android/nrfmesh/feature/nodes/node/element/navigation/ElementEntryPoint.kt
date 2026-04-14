package no.nordicsemi.android.nrfmesh.feature.nodes.node.element.navigation

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
import no.nordicsemi.android.nrfmesh.core.navigation.NodeListDetailSceneKey
import no.nordicsemi.android.nrfmesh.core.ui.isCompactWidth
import no.nordicsemi.android.nrfmesh.feature.model.navigation.ModelKey
import no.nordicsemi.android.nrfmesh.feature.model.navigation.modelEntry
import no.nordicsemi.android.nrfmesh.feature.nodes.node.element.ElementScreen
import no.nordicsemi.android.nrfmesh.feature.nodes.node.element.ElementViewModel
import no.nordicsemi.kotlin.mesh.core.model.Address
import kotlin.uuid.ExperimentalUuidApi

@Serializable
data class ElementKey(val address: Address) : NavKey

@OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.elementEntry(appState: AppState, navigator: Navigator) {
    entry<ElementKey>(
        metadata = ListDetailSceneStrategy.detailPane(
            sceneKey = NodeListDetailSceneKey
        )
    ) { key ->
        val address = key.address
        val viewModel = hiltViewModel<ElementViewModel, ElementViewModel.Factory> {
            it.create(address = address.toInt())
        }
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ElementScreen(
            elementState = uiState.elementState,
            highlightSelectedItem = !isCompactWidth() && appState.navigationState.currentKey is ModelKey,
            navigateToModel = { model ->
                navigator.navigate(
                    key = ModelKey(
                        address = address,
                        modelId = model.modelId.id
                    )
                )
            },
            save = viewModel::save
        )
    }
    modelEntry(appState = appState, navigator = navigator)
}