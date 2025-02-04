package no.nordicsemi.android.nrfmesh.feature.settings.navigation

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
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeyRoute
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeysRoute
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeyRoute
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeysRoute
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.ProvisionerRoute
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.ProvisionersRoute
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.SceneRoute
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.ScenesRoute
import no.nordicsemi.android.nrfmesh.feature.settings.ClickableSetting
import no.nordicsemi.android.nrfmesh.feature.settings.MeshNetworkState
import no.nordicsemi.android.nrfmesh.feature.settings.SettingsList
import no.nordicsemi.android.nrfmesh.feature.settings.SettingsListDetails
import no.nordicsemi.android.nrfmesh.feature.settings.SettingsListDetailsExtra
import no.nordicsemi.android.nrfmesh.feature.settings.SettingsScreenUiState


@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun SettingsListDetailsScreen(
    uiState: SettingsScreenUiState,
    onItemSelected: (ClickableSetting) -> Unit,
    onNameChanged: (String) -> Unit,
    save: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val navigator = rememberListDetailPaneScaffoldNavigator<Any>(isDestinationHistoryAware = false)
    when (uiState.networkState) {
        is MeshNetworkState.Success -> NavigableListDetailPaneScaffold(
            navigator = navigator,
            defaultBackBehavior = BackNavigationBehavior.PopUntilScaffoldValueChange,
            listPane = {
                AnimatedPane {
                    SettingsList(
                        settingsListData = uiState.networkState.settingsListData,
                        selectedSetting = uiState.selectedSetting,
                        highlightSelectedItem = navigator.isListPaneVisible(),
                        onNameChanged = onNameChanged,
                        navigateToProvisioners = {
                            onItemSelected(ClickableSetting.Provisioners)
                            scope.launch {
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Detail,
                                    contentKey = ProvisionersRoute
                                )
                            }
                        },
                        navigateToNetworkKeys = {
                            onItemSelected(ClickableSetting.NetworkKeys)
                            scope.launch {
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Detail,
                                    contentKey = NetworkKeysRoute
                                )
                            }
                        },
                        navigateToApplicationKeys = {
                            onItemSelected(ClickableSetting.ApplicationKeys)
                            scope.launch {
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Detail,
                                    contentKey = ApplicationKeysRoute
                                )
                            }
                        },
                        navigateToScenes = {
                            onItemSelected(ClickableSetting.Scenes)
                            scope.launch {
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Detail,
                                    contentKey = ScenesRoute
                                )
                            }
                        },
                    )
                }
            },
            detailPane = {
                AnimatedPane {
                    val content = navigator.currentDestination?.contentKey
                    SettingsListDetails(
                        content = content,
                        highlightSelectedItem = navigator.isDetailPaneVisible() &&
                                navigator.isExtraPaneVisible(),
                        navigateToProvisioner = {
                            scope.launch {
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Extra,
                                    contentKey = ProvisionerRoute(uuid = it)
                                )
                            }
                        },
                        navigateToNetworkKey = {
                            scope.launch {
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Extra,
                                    contentKey = NetworkKeyRoute(keyIndex = it)
                                )
                            }
                        },
                        navigateToApplicationKey = {
                            scope.launch {
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Extra,
                                    contentKey = ApplicationKeyRoute(keyIndex = it)
                                )
                            }
                        },
                        navigateToScene = {
                            scope.launch {
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Extra,
                                    contentKey = SceneRoute(number = it)
                                )
                            }
                        },
                        navigateUp = {
                            scope.launch {
                                if (navigator.isExtraPaneVisible()) {
                                    navigator.navigateBack()
                                }
                            }
                        }
                    )
                }
            },
            extraPane = {
                AnimatedPane {
                    val content = navigator.currentDestination?.contentKey
                    SettingsListDetailsExtra(
                        network = uiState.networkState.network,
                        content = content,
                        save = save
                    )
                }
            }
        )

        else -> {
            // Do something else
        }
    }
}