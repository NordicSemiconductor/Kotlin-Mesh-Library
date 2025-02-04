package no.nordicsemi.android.nrfmesh.feature.nodes.navigation

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
import no.nordicsemi.android.nrfmesh.feature.nodes.ClickableNodeInfoItem
import no.nordicsemi.android.nrfmesh.feature.nodes.NodeInfoList
import no.nordicsemi.android.nrfmesh.feature.nodes.NodeInfoListDetails
import no.nordicsemi.android.nrfmesh.feature.nodes.NodeInfoListDetailsExtra
import no.nordicsemi.android.nrfmesh.feature.nodes.NodeScreenUiState
import no.nordicsemi.android.nrfmesh.feature.nodes.NodeState
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalStdlibApi::class)
@Composable
internal fun NodeListDetailsScreen(
    uiState: NodeScreenUiState,
    onRefresh: () -> Unit,
    onGetTtlClicked: () -> Unit,
    onProxyStateToggled: (Boolean) -> Unit,
    onGetProxyStateClicked: () -> Unit,
    onExcluded: (Boolean) -> Unit,
    onResetClicked: () -> Unit,
    onItemSelected: (ClickableNodeInfoItem) -> Unit,
    send: (AcknowledgedConfigMessage) -> Unit,
    save: () -> Unit,
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
                                    onItemSelected(ClickableNodeInfoItem.Element)
                                    navigator.navigateTo(
                                        pane = ListDetailPaneScaffoldRole.Detail,
                                        contentKey = ElementRoute(address = it)
                                    )
                                }
                            },
                            onGetTtlClicked = onGetTtlClicked,
                            onProxyStateToggled = onProxyStateToggled,
                            onGetProxyStateClicked = onGetProxyStateClicked,
                            onExcluded = onExcluded,
                            onResetClicked = onResetClicked,
                            selectedItem = uiState.selectedNodeInfoItem,
                            save = save
                        )
                    }
                },
                detailPane = {
                    AnimatedPane {
                        val content = navigator.currentDestination?.contentKey
                        NodeInfoListDetails(
                            content = content,
                            node = uiState.nodeState.node,
                            highlightSelectedItem = navigator.isDetailPaneVisible(),
                            navigateToModel = {
                                scope.launch {
                                    navigator.navigateTo(
                                        pane = ListDetailPaneScaffoldRole.Extra,
                                        contentKey = ModelRoute(
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
                            highlightSelectedItem = navigator.isExtraPaneVisible(),
                            send = send
                        )
                    }
                }
            )
        }

        else -> {}
    }

}