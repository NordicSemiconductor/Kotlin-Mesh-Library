package no.nordicsemi.android.nrfmesh.feature.groups

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.ui.isDetailPaneVisible
import no.nordicsemi.kotlin.mesh.core.messages.UnacknowledgedMeshMessage
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.Group

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalStdlibApi::class)
@Composable
internal fun GroupListDetailScreen(
    appState: AppState,
    snackbarHostState: SnackbarHostState,
    uiState: GroupState,
    onModelClicked: (Int) -> Unit,
    send: (UnacknowledgedMeshMessage, ApplicationKey) -> Unit,
    deleteGroup: (Group) -> Unit,
    save: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val navigator = appState.groupsNavigator
    when (uiState) {
        is GroupState.Success -> {
            NavigableListDetailPaneScaffold(
                navigator = navigator,
                listPane = {
                    AnimatedPane {
                        GroupListPane(
                            snackbarHostState = snackbarHostState,
                            groupInfo = uiState.groupInfoListData,
                            group = uiState.group,
                            onModelClicked = { modelId, index ->
                                onModelClicked(index)
                                scope.launch {
                                    navigator.navigateTo(
                                        pane = ListDetailPaneScaffoldRole.Detail,
                                        contentKey = ModelControls(id = modelId.id.toHexString())
                                    )
                                }
                            },
                            isDetailPaneVisible = navigator.isDetailPaneVisible(),
                            selectedModelIndex = uiState.selectedModelIndex,
                            deleteGroup = deleteGroup,
                            save = save
                        )
                    }
                },
                detailPane = {
                    AnimatedPane {
                        val content = navigator.currentDestination?.contentKey
                        GroupDetailPane(
                            content = content,
                            network = uiState.network,
                            models = uiState.groupInfoListData.models,
                            send = send
                        )
                    }
                }
            )
        }

        else -> {
            // Error
        }
    }
}