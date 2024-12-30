package no.nordicsemi.android.nrfmesh.feature.settings.navigation

import android.content.ContentResolver
import android.net.Uri
import android.os.Parcelable
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirectiveWithTwoPanesOnMediumWidth
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.NavigableSupportingPaneScaffold
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.core.navigation.SettingsItemRoute
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeyRoute
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeysRoute
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.ProvisionerRoute
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.ProvisionersRoute
import no.nordicsemi.android.nrfmesh.feature.settings.MeshNetworkState
import no.nordicsemi.android.nrfmesh.feature.settings.SettingsRoute
import no.nordicsemi.android.nrfmesh.feature.settings.SettingsScreenUiState
import no.nordicsemi.android.nrfmesh.feature.settings.SettingsViewModel


@Serializable
internal data object TopicPlaceholderRoute

@Serializable
internal data object DetailPaneNavHostRoute

@Serializable
@Parcelize
data object SettingsRoute : Parcelable

object SettingsDestination : MeshNavigationDestination {
    override val route: String = "settings_route"
    override val destination: String = "settings_destination"
}

const val SETTINGS_ROUTE = "settings_route"

fun NavController.navigateToSettings(navOptions: NavOptions) = navigate(
    route = SettingsRoute,
    navOptions = navOptions
)

internal fun NavController.navigateToSettingsItem(
    settingsItemRoute: String, navOptions: NavOptionsBuilder.() -> Unit = {}
) = navigate(
    route = SettingsItemRoute(
        settingsItemRoute
    )
) {
    navOptions()
}

fun NavGraphBuilder.settingsListDetailsScreen(appState: AppState) {
    composable<SettingsRoute> {
        val viewModel = hiltViewModel<SettingsViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        SettingsListDetailsScreen(
            appState = appState,
            uiState = uiState,
            onNameChanged = viewModel::onNameChanged,
            importNetwork = viewModel::importNetwork,
            resetNetwork = viewModel::resetNetwork
        )
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun navigateToDefaultDestination(
    navigator: ThreePaneScaffoldNavigator<Any>,
    scope: CoroutineScope,
    pane: ThreePaneScaffoldRole,
    destination: Any
) {
    scope.launch {
        navigator.navigateTo(
            pane = pane,
            contentKey = destination
        )
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SettingsListDetailsScreen(
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
                    Column(modifier = Modifier.fillMaxSize()) {
                        SettingsRoute(
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
                    }
                },
                detailPane = {
                    AnimatedPane {
                        val content = navigator.currentDestination?.contentKey
                        DetailPaneContent(
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
                        Box(Modifier.background(Color.Blue)) {
                            ExtraPaneContent(
                                scope = scope,
                                navigator = navigator,
                                appState = appState,
                                network = uiState.networkState.network,
                                content = it
                            )
                        }
                    }
                }
            )
        }

        else -> {
            // Do something else
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun <T> ThreePaneScaffoldNavigator<T>.isListPaneVisible(): Boolean =
    scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Expanded

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun <T> ThreePaneScaffoldNavigator<T>.isDetailPaneVisible(): Boolean =
    scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun <T> ThreePaneScaffoldNavigator<T>.isExtraPaneVisible(): Boolean =
    scaffoldValue[ListDetailPaneScaffoldRole.Extra] == PaneAdaptedValue.Expanded

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun getScaffoldDirective(windowSize: WindowSizeClass): PaneScaffoldDirective {
    return when (windowSize.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            println("Compact")
            PaneScaffoldDirective(
                maxHorizontalPartitions = 1,
                horizontalPartitionSpacerSize = 0.dp,
                maxVerticalPartitions = 1,
                verticalPartitionSpacerSize = 0.dp,
                defaultPanePreferredWidth = 250.dp,
                excludedBounds = emptyList()
            )
        }

        WindowWidthSizeClass.Medium -> {
            println("Medium")
            calculatePaneScaffoldDirectiveWithTwoPanesOnMediumWidth(
                currentWindowAdaptiveInfo()
            )
        }

        WindowWidthSizeClass.Expanded -> {
            println("Expanded")
            PaneScaffoldDirective(
                maxHorizontalPartitions = 2,
                horizontalPartitionSpacerSize = 0.dp,
                maxVerticalPartitions = 1,
                verticalPartitionSpacerSize = 0.dp,
                defaultPanePreferredWidth = 350.dp,
                excludedBounds = emptyList()
            )
        }

        else -> calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())
    }
}