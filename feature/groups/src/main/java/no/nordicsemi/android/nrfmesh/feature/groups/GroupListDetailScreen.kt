package no.nordicsemi.android.nrfmesh.feature.groups

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.common.MessageState

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalStdlibApi::class)
@Composable
internal fun GroupListDetailScreen(
    @Suppress("UNUSED_PARAMETER") messageState: MessageState,
    uiState: GroupState,
    save: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val navigator = rememberListDetailPaneScaffoldNavigator<Any>()
    when (uiState) {
        is GroupState.Success -> {
            NavigableListDetailPaneScaffold(
                navigator = navigator,
                listPane = {
                    AnimatedPane {
                        GroupListPane(
                            groupInfo = uiState.groupInfoListData,
                            group = uiState.group,
                            onModelClicked = {
                                scope.launch {
                                    navigator.navigateTo(
                                        pane = ListDetailPaneScaffoldRole.Detail,
                                        contentKey = ModelControls(id = it.id.toHexString())
                                    )
                                }
                            },
                            save = save,
                        )
                    }
                },
                detailPane = {
                    AnimatedPane {
                        val content = navigator.currentDestination?.contentKey
                        GroupDetailPane(
                            content = content,
                            network = uiState.network,
                            group = uiState.group,
                            models = uiState.groupInfoListData.models
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