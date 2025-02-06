package no.nordicsemi.android.nrfmesh.feature.nodes.node

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.ui.isDetailPaneVisible
import no.nordicsemi.android.nrfmesh.core.ui.isExtraPaneVisible
import no.nordicsemi.android.nrfmesh.core.ui.isListPaneVisible
import no.nordicsemi.android.nrfmesh.feature.nodes.node.navigation.ElementRouteKeyKey
import no.nordicsemi.android.nrfmesh.feature.nodes.node.navigation.ModelRouteKeyKey
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.model.Model

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun NodeListDetailsScreen(
    uiState: NodeScreenUiState,
    onRefresh: () -> Unit,
    onExcluded: (Boolean) -> Unit,
    onItemSelected: (ClickableNodeInfoItem) -> Unit,
    send: (AcknowledgedConfigMessage) -> Unit,
    requestNodeIdentityStates: (Model) -> Unit,
    save: () -> Unit,
    resetMessageState: () -> Unit,
    navigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val navigator = rememberListDetailPaneScaffoldNavigator<Any>()
    when (uiState.nodeState) {
        is NodeState.Success -> {
            NavigableListDetailPaneScaffold(
                navigator = navigator,
                defaultBackBehavior = BackNavigationBehavior.PopUntilScaffoldValueChange,
                listPane = {
                    AnimatedPane {
                        NodeInfoList(
                            messageState = uiState.messageState,
                            nodeData = uiState.nodeState.nodeInfoListData,
                            node = uiState.nodeState.node,
                            highlightSelectedItem = navigator.isListPaneVisible(),
                            isRefreshing = uiState.isRefreshing,
                            onRefresh = onRefresh,
                            onNetworkKeysClicked = {
                                onItemSelected(ClickableNodeInfoItem.NetworkKeys)
                            },
                            onApplicationKeysClicked = {
                                onItemSelected(ClickableNodeInfoItem.ApplicationKeys)
                            },
                            onElementClicked = {
                                scope.launch {
                                    onItemSelected(ClickableNodeInfoItem.Element(it))
                                    navigator.navigateTo(
                                        pane = ListDetailPaneScaffoldRole.Detail,
                                        contentKey = ElementRouteKeyKey(address = it)
                                    )
                                }
                            },
                            onExcluded = onExcluded,
                            selectedItem = uiState.selectedNodeInfoItem,
                            send = send,
                            save = save,
                            navigateBack = navigateBack
                        )
                    }
                },
                detailPane = {
                    AnimatedPane {
                        val content = navigator.currentDestination?.contentKey
                        NodeInfoListDetails(
                            content = content,
                            node = uiState.nodeState.node,
                            highlightSelectedItem = navigator.isDetailPaneVisible() &&
                                    navigator.isExtraPaneVisible(),
                            navigateToModel = {
                                scope.launch {
                                    navigator.navigateTo(
                                        pane = ListDetailPaneScaffoldRole.Extra,
                                        contentKey = ModelRouteKeyKey(
                                            modelId = it.modelId.id,
                                            address = it.parentElement?.unicastAddress?.address
                                                ?: throw IllegalStateException()
                                        )
                                    )
                                }
                            },
                            save = save
                        )
                    }
                },
                extraPane = {
                    AnimatedPane {
                        val content = navigator.currentDestination?.contentKey
                        NodeInfoListDetailsExtra(
                            node = uiState.nodeState.node,
                            messageState = uiState.messageState,
                            nodeIdentityStatus = uiState.nodeIdentityStates,
                            content = content,
                            send = send,
                            resetMessageState = resetMessageState,
                            requestNodeIdentityStates = requestNodeIdentityStates
                        )
                    }
                }
            )
        }

        else -> {}
    }

}