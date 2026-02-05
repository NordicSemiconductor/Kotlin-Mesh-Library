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
import no.nordicsemi.android.nrfmesh.feature.model.navigation.ModelKey
import no.nordicsemi.android.nrfmesh.feature.model.navigation.modelEntry
import no.nordicsemi.android.nrfmesh.feature.nodes.node.element.ElementScreen
import no.nordicsemi.android.nrfmesh.feature.nodes.node.element.ElementViewModel
import no.nordicsemi.kotlin.data.HexString
import kotlin.uuid.ExperimentalUuidApi

@Serializable
data class ElementKey(val address: HexString) : NavKey

@OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.elementEntry(appState: AppState, navigator: Navigator) {
    entry<ElementKey>(
        metadata = ListDetailSceneStrategy.detailPane()
    ) { key ->
        val address = key.address
        val viewModel = hiltViewModel<ElementViewModel, ElementViewModel.Factory>(key = address) {
            it.create(address = address)
        }
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ElementScreen(
            elementState = uiState.elementState,
            highlightSelectedItem = !isCompactWidth(),
            navigateToModel = {
                navigator.navigate(
                    key = ModelKey(
                        address = address,
                        modelId = it.modelId.id.toHexString()
                    )
                )
            },
            save = viewModel::save
        )
    }
    modelEntry(appState = appState, navigator = navigator)
}