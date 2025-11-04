package no.nordicsemi.android.nrfmesh.feature.settings.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.ClickableSetting
import no.nordicsemi.android.nrfmesh.core.ui.isDetailPaneVisible
import no.nordicsemi.android.nrfmesh.core.ui.isExtraPaneVisible
import no.nordicsemi.android.nrfmesh.core.ui.isListPaneVisible
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeyContent
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeysContent
import no.nordicsemi.android.nrfmesh.feature.ivindex.navigation.IvIndexContent
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeyContent
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeysContent
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.ProvisionerContent
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.ProvisionersContent
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.SceneContent
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.ScenesContent
import no.nordicsemi.android.nrfmesh.feature.settings.MeshNetworkState
import no.nordicsemi.android.nrfmesh.feature.settings.SettingsDetailsPane
import no.nordicsemi.android.nrfmesh.feature.settings.SettingsExtraPane
import no.nordicsemi.android.nrfmesh.feature.settings.SettingsListPane
import no.nordicsemi.android.nrfmesh.feature.settings.SettingsScreenUiState
import no.nordicsemi.kotlin.mesh.core.model.Provisioner


@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun SettingsListDetailsScreen(
    appState: AppState,
    uiState: SettingsScreenUiState,
    onItemSelected: (ClickableSetting) -> Unit,
    onNameChanged: (String) -> Unit,
    moveProvisioner: (Provisioner, Int) -> Unit,
    save: () -> Unit,
    onBackPressed: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val navigator = appState.settingsNavigator
    when (uiState.networkState) {
        is MeshNetworkState.Success -> NavigableListDetailPaneScaffold(
            navigator = navigator,
            defaultBackBehavior = BackNavigationBehavior.PopUntilScaffoldValueChange,
            listPane = {
                AnimatedPane {
                    SettingsListPane(
                        settingsListData = uiState.networkState.settingsListData,
                        selectedSetting = uiState.selectedSetting,
                        highlightSelectedItem = navigator.isListPaneVisible(),
                        onNameChanged = onNameChanged,
                        navigateToProvisioners = {
                            onItemSelected(ClickableSetting.PROVISIONERS)
                            scope.launch {
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Detail,
                                    contentKey = ProvisionersContent
                                )
                            }
                        },
                        navigateToNetworkKeys = {
                            onItemSelected(ClickableSetting.NETWORK_KEYS)
                            scope.launch {
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Detail,
                                    contentKey = NetworkKeysContent
                                )
                            }
                        },
                        navigateToApplicationKeys = {
                            onItemSelected(ClickableSetting.APPLICATION_KEYS)
                            scope.launch {
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Detail,
                                    contentKey = ApplicationKeysContent
                                )
                            }
                        },
                        navigateToScenes = {
                            onItemSelected(ClickableSetting.SCENES)
                            scope.launch {
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Detail,
                                    contentKey = ScenesContent
                                )
                            }
                        },
                        navigateToIvIndex = {
                            onItemSelected(ClickableSetting.IV_INDEX)
                            scope.launch {
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Detail,
                                    contentKey = IvIndexContent
                                )
                            }
                        },
                    )
                }
            },
            detailPane = {
                AnimatedPane {
                    val content = navigator.currentDestination?.contentKey
                    SettingsDetailsPane(
                        content = content,
                        highlightSelectedItem = navigator.isDetailPaneVisible() &&
                                navigator.isExtraPaneVisible(),
                        navigateToProvisioner = {
                            scope.launch {
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Extra,
                                    contentKey = ProvisionerContent(uuid = it.toString())
                                )
                            }
                        },
                        navigateToNetworkKey = {
                            scope.launch {
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Extra,
                                    contentKey = NetworkKeyContent(keyIndex = it)
                                )
                            }
                        },
                        navigateToApplicationKey = {
                            scope.launch {
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Extra,
                                    contentKey = ApplicationKeyContent(keyIndex = it)
                                )
                            }
                        },
                        navigateToScene = {
                            scope.launch {
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Extra,
                                    contentKey = SceneContent(number = it)
                                )
                            }
                        },
                        navigateUp = {
                            scope.launch {
                                if (navigator.isExtraPaneVisible()) {
                                    navigator.navigateBack()
                                } else {
                                    onBackPressed()
                                }
                            }
                        }
                    )
                }
            },
            extraPane = {
                AnimatedPane {
                    val content = navigator.currentDestination?.contentKey
                    SettingsExtraPane(
                        network = uiState.networkState.network,
                        settingsListData = uiState.networkState.settingsListData,
                        content = content,
                        moveProvisioner = moveProvisioner,
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