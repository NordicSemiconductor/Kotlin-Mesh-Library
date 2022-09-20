package no.nordicsemi.android.nrfmesh.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeyDestination
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeysDestination
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.applicationKeysGraph
import no.nordicsemi.android.feature.scenes.navigation.SceneDestination
import no.nordicsemi.android.feature.scenes.navigation.ScenesDestination
import no.nordicsemi.android.feature.scenes.navigation.scenesGraph
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.export.navigation.ExportDestination
import no.nordicsemi.android.nrfmesh.feature.export.navigation.exportGraph
import no.nordicsemi.android.nrfmesh.feature.groups.navigation.groupsGraph
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeyDestination
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeysDestination
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.networkKeysGraph
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.NodesDestination
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.nodesGraph
import no.nordicsemi.android.nrfmesh.feature.proxyfilter.navigation.proxyFilterGraph
import no.nordicsemi.android.nrfmesh.feature.settings.navigation.settingsGraph

@Composable
fun MeshNavHost(
    navController: NavHostController,
    onNavigateToDestination: (MeshNavigationDestination, String) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    startDestination: String = NodesDestination.route
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        nodesGraph()
        groupsGraph()
        proxyFilterGraph()
        settingsGraph(
            navigateToExportNetwork = {
                onNavigateToDestination(ExportDestination, ExportDestination.route)
            },
            navigateToProvisioners = {
                // onNavigateToDestination(NetworkKeysDestination, NetworkKeysDestination.route)
            },
            navigateToNetworkKeys = {
                onNavigateToDestination(NetworkKeysDestination, NetworkKeysDestination.route)
            },
            navigateToApplicationKeys = {
                onNavigateToDestination(
                    ApplicationKeysDestination,
                    ApplicationKeysDestination.route
                )
            },
            navigateToScenes = {
                onNavigateToDestination(ScenesDestination, ScenesDestination.route)
            },
            nestedGraphs = {
                exportGraph(onBackPressed = onBackPressed)
                networkKeysGraph(
                    onBackPressed = onBackPressed,
                    onNavigateToNetworkKey = { netKeyIndex ->
                        onNavigateToDestination(
                            NetworkKeyDestination,
                            NetworkKeyDestination.createNavigationRoute(
                                netKeyIndexArg = netKeyIndex
                            )
                        )
                    }
                )
                applicationKeysGraph(
                    onBackPressed = onBackPressed,
                    onNavigateToApplicationKey = { appKeyIndex ->
                        onNavigateToDestination(
                            ApplicationKeyDestination,
                            ApplicationKeyDestination.createNavigationRoute(
                                appKeyIndexArg = appKeyIndex
                            )
                        )
                    }
                )
                scenesGraph(
                    onBackPressed = onBackPressed,
                    onNavigateToScene = { sceneNumber ->
                        onNavigateToDestination(
                            SceneDestination,
                            SceneDestination.createNavigationRoute(
                                sceneNumberArg = sceneNumber
                            )
                        )
                    }
                )
            }
        )
    }
}