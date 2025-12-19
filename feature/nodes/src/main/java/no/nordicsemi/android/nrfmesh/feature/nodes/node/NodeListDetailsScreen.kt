package no.nordicsemi.android.nrfmesh.feature.nodes.node

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import no.nordicsemi.android.feature.config.networkkeys.navigation.ConfigNetKeysRoute
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.ClickableSetting
import no.nordicsemi.android.nrfmesh.core.ui.isListPaneVisible
import no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.navigation.ConfigAppKeysRoute
import no.nordicsemi.android.nrfmesh.feature.groups.navigation.navigateToGroups
import no.nordicsemi.android.nrfmesh.feature.nodes.node.navigation.ElementRouteKey
import no.nordicsemi.android.nrfmesh.feature.nodes.node.navigation.ModelRouteKey
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.MeshMessage
import no.nordicsemi.kotlin.mesh.core.model.Model
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalUuidApi::class)
@Composable
internal fun NodeListDetailsScreen(
    appState: AppState,
    uiState: NodeScreenUiState,
    onRefresh: () -> Unit,
    onExcluded: (Boolean) -> Unit,
    onItemSelected: (ClickableNodeInfoItem) -> Unit,
    onAddNetworkKeyClicked: () -> Unit,
    onAddAppKeyClicked: () -> Unit,
    requestNodeIdentityStates: (Model) -> Unit,
    send: (AcknowledgedConfigMessage) -> Unit,
    sendApplicationMessage: (Model, MeshMessage) -> Unit,
    resetMessageState: () -> Unit,
    save: () -> Unit,
    navigateBack: () -> Unit,
    removeNode: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val navigator = rememberListDetailPaneScaffoldNavigator().also {
        appState.nodeNavigator = it
    }
    when (uiState.nodeState) {
        is NodeState.Success -> NavigableListDetailPaneScaffold(
            modifier = Modifier.windowInsetsPadding(WindowInsets()),
            navigator = navigator,
            listPane = {
                AnimatedPane {
                    NodeListPane(
                        messageState = uiState.messageState,
                        nodeData = uiState.nodeState.nodeInfoListData,
                        node = uiState.nodeState.node,
                        highlightSelectedItem = navigator.isListPaneVisible(),
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = onRefresh,
                        onNetworkKeysClicked = {
                            scope.launch {
                                onItemSelected(ClickableNodeInfoItem.NetworkKeys)
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Detail,
                                    contentKey = ConfigNetKeysRoute
                                )
                            }
                        },
                        onApplicationKeysClicked = {
                            scope.launch {
                                onItemSelected(ClickableNodeInfoItem.ApplicationKeys)
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Detail,
                                    contentKey = ConfigAppKeysRoute
                                )
                            }
                        },
                        onElementClicked = {
                            scope.launch {
                                onItemSelected(ClickableNodeInfoItem.Element(it))
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Detail,
                                    contentKey = ElementRouteKey(address = it)
                                )
                            }
                        },
                        onExcluded = onExcluded,
                        selectedItem = uiState.selectedNodeInfoItem,
                        send = send,
                        save = save,
                        navigateBack = navigateBack,
                        removeNode = removeNode
                    )
                }
            },
            detailPane = {
                AnimatedPane {
                    NodeDetailsPane(
                        navigator = navigator,
                        node = uiState.nodeState.node,
                        availableNetworkKeys = uiState.availableNetworkKeys,
                        onAddNetworkKeyClicked = onAddNetworkKeyClicked,
                        navigateToNetworkKeys = {
                            appState.navigateToSettings(
                                listItem = ClickableSetting.NETWORK_KEYS
                            )
                        },
                        availableApplicationKeys = uiState.availableAppKeys,
                        onAddAppKeyClicked = onAddAppKeyClicked,
                        navigateToApplicationKeys = {
                            appState.navigateToSettings(
                                listItem = ClickableSetting.APPLICATION_KEYS
                            )
                        },
                        navigateToModel = {
                            scope.launch {
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Extra,
                                    contentKey = ModelRouteKey(
                                        modelId = it.modelId.id,
                                        address = it.parentElement?.unicastAddress?.address
                                            ?: throw IllegalStateException()
                                    )
                                )
                            }
                        },
                        send = send,
                        messageState = uiState.messageState,
                        resetMessageState = resetMessageState,
                        save = save
                    )
                }
            },
            extraPane = {
                AnimatedPane {
                    val content = navigator.currentDestination?.contentKey
                    NodeExtraPane(
                        snackbarHostState = appState.snackbarHostState,
                        node = uiState.nodeState.node,
                        messageState = uiState.messageState,
                        nodeIdentityStatus = uiState.nodeIdentityStates,
                        content = content,
                        send = send,
                        sendApplicationMessage = sendApplicationMessage,
                        resetMessageState = resetMessageState,
                        requestNodeIdentityStates = requestNodeIdentityStates,
                        navigateToGroups = { appState.navController.navigateToGroups() },
                        navigateToConfigApplicationKeys = {
                            scope.launch {
                                onItemSelected(ClickableNodeInfoItem.ApplicationKeys)
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.List,
                                    contentKey = ConfigAppKeysRoute
                                )
                            }
                        }
                    )
                }
            }
        )

        else -> {
            // Do nothing, waiting for the node to be loaded.
        }
    }
}