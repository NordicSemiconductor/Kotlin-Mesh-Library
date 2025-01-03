package no.nordicsemi.android.nrfmesh.feature.settings.navigation

import android.content.ContentResolver
import android.net.Uri
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeyRoute
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeysRoute
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.ProvisionerRoute
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.ProvisionersRoute
import no.nordicsemi.android.nrfmesh.feature.settings.MeshNetworkState
import no.nordicsemi.android.nrfmesh.feature.settings.SettingsList
import no.nordicsemi.android.nrfmesh.feature.settings.SettingsListDetails
import no.nordicsemi.android.nrfmesh.feature.settings.SettingsListDetailsExtra
import no.nordicsemi.android.nrfmesh.feature.settings.SettingsScreenUiState


@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun SettingsListDetailsScreen(
    appState: AppState,
    uiState: SettingsScreenUiState,
    onNameChanged: (String) -> Unit,
    importNetwork: (uri: Uri, contentResolver: ContentResolver) -> Unit,
    resetNetwork: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val navigator = rememberListDetailPaneScaffoldNavigator<Any>()

    when (uiState.networkState) {
        is MeshNetworkState.Success -> {
            NavigableListDetailPaneScaffold(
                navigator = navigator,
                listPane = {
                    SettingsList(
                        appState = appState,
                        uiState = uiState,
                        onNameChanged = onNameChanged,
                        navigateToProvisioners = {
                            scope.launch {
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Detail,
                                    contentKey = ProvisionersRoute
                                )
                            }
                        },
                        navigateToNetworkKeys = {
                            scope.launch {
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Detail,
                                    contentKey = NetworkKeysRoute
                                )
                            }
                        },
                        navigateToApplicationKeys = {},
                        navigateToScenes = {},
                        importNetwork = importNetwork,
                        navigateToExport = {},
                        resetNetwork = resetNetwork
                    )
                },
                detailPane = {
                    AnimatedPane {
                        val content = navigator.currentDestination?.contentKey
                        SettingsListDetails(
                            content = content,
                            appState = appState,
                            network = uiState.networkState.network,
                            navigateToProvisioner = {
                                scope.launch {
                                    navigator.navigateTo(
                                        pane = ListDetailPaneScaffoldRole.Extra,
                                        contentKey = ProvisionerRoute(uuid = it.uuid)
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
                            onBackPressed = { scope.launch { navigator.navigateBack() } }
                        )
                    }
                },
                extraPane = {
                    navigator.currentDestination?.contentKey?.let {
                        SettingsListDetailsExtra(
                            scope = scope,
                            navigator = navigator,
                            appState = appState,
                            network = uiState.networkState.network,
                            content = it
                        )
                    }
                }
            )
        }

        else -> {
            // Do something else
        }
    }
}