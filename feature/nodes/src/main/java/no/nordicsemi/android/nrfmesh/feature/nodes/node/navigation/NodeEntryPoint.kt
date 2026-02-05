package no.nordicsemi.android.nrfmesh.feature.nodes.node.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import no.nordicsemi.android.feature.config.networkkeys.ConfigNetKeysKey
import no.nordicsemi.android.feature.config.networkkeys.configNetKeysEntry
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.Navigator
import no.nordicsemi.android.nrfmesh.core.navigation.NodeKey
import no.nordicsemi.android.nrfmesh.core.navigation.NodesKey
import no.nordicsemi.android.nrfmesh.core.ui.PlaceHolder
import no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.ConfigAppKeysKey
import no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.configAppKeysEntry
import no.nordicsemi.android.nrfmesh.feature.nodes.R
import no.nordicsemi.android.nrfmesh.feature.nodes.node.ClickableNodeInfoItem
import no.nordicsemi.android.nrfmesh.feature.nodes.node.NodeScreen
import no.nordicsemi.android.nrfmesh.feature.nodes.node.NodeState
import no.nordicsemi.android.nrfmesh.feature.nodes.node.NodeViewModel
import no.nordicsemi.android.nrfmesh.feature.nodes.node.element.navigation.ElementKey
import no.nordicsemi.android.nrfmesh.feature.nodes.node.element.navigation.elementEntry
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.nodeEntry(
    appState: AppState,
    navigator: Navigator,
) {
    entry<NodeKey>(
        metadata = ListDetailSceneStrategy.listPane {
            PlaceHolder(
                modifier = Modifier.fillMaxSize(),
                imageVector = Icons.Outlined.Info,
                text = stringResource(R.string.label_select_node_item_rationale)
            )
        }
    ) { key ->
        val uuid = key.nodeUuid
        val viewModel = hiltViewModel<NodeViewModel, NodeViewModel.Factory>(key = uuid) {
            it.create(uuid = uuid)
        }
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        when (uiState.nodeState) {
            is NodeState.Success -> {
                NodeScreen(
                    messageState = uiState.messageState,
                    nodeData = (uiState.nodeState as NodeState.Success).nodeInfoListData,
                    node = (uiState.nodeState as NodeState.Success).node,
                    highlightSelectedItem = !isCompactWidth(),
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = viewModel::onRefresh,
                    onNetworkKeysClicked = {
                        viewModel.onItemSelected(item = ClickableNodeInfoItem.NetworkKeys)
                        navigator.navigate(key = ConfigNetKeysKey(uuid = it.toString()))
                    },
                    onApplicationKeysClicked = {
                        viewModel.onItemSelected(item = ClickableNodeInfoItem.ApplicationKeys)
                        navigator.navigate(key = ConfigAppKeysKey(uuid = it.toString()))
                    },
                    onElementClicked = {
                        viewModel.onItemSelected(item = ClickableNodeInfoItem.Element(address = it))
                        navigator.navigate(key = ElementKey(address = it.toHexString()))
                    },
                    onExcluded = viewModel::onExcluded,
                    selectedItem = uiState.selectedNodeInfoItem,
                    send = viewModel::send,
                    save = viewModel::save,
                    navigateBack = { navigator.navigate(key = NodesKey) },
                    removeNode = viewModel::removeNode
                )
            }

            else -> {
                // Do nothing wait for it to be loaded
            }
        }
    }
    configNetKeysEntry(appState = appState, navigator = navigator)
    configAppKeysEntry(appState = appState, navigator = navigator)
    elementEntry(appState = appState, navigator = navigator)
}