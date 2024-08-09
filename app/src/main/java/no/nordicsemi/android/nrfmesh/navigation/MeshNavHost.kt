package no.nordicsemi.android.nrfmesh.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeyDestination
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeysDestination
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.applicationKeysGraph
import no.nordicsemi.android.nrfmesh.feature.export.navigation.ExportDestination
import no.nordicsemi.android.nrfmesh.feature.export.navigation.exportGraph
import no.nordicsemi.android.nrfmesh.feature.groups.navigation.groupsGraph
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeyDestination
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeysDestination
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.networkKeysGraph
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.NodeDestination
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.NodesDestination
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.nodesGraph
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.ProvisionerDestination
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.ProvisionersDestination
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.provisionersGraph
import no.nordicsemi.android.nrfmesh.feature.proxy.navigation.proxyFilterGraph
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.SceneDestination
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.ScenesDestination
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.scenesGraph
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
        nodesGraph(
            navigateToNode = { node ->
                onNavigateToDestination(
                    NodeDestination,
                    NodeDestination.createNavigationRoute(node.uuid)
                )
            },
            onBackPressed = onBackPressed
        )
        groupsGraph(
            navigateToGroup = { group ->

            }
        )
        proxyFilterGraph()
        settingsGraph(
            navigateToExportNetwork = {
                onNavigateToDestination(ExportDestination, ExportDestination.route)
            },
            navigateToProvisioners = {
                onNavigateToDestination(ProvisionersDestination, ProvisionersDestination.route)
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
                exportGraph(/*onBackPressed = onBackPressed*/)
                provisionersGraph(
                    onBackPressed = onBackPressed,
                    onNavigateToProvisioner = { provisionerUuid ->
                        onNavigateToDestination(
                            ProvisionerDestination,
                            ProvisionerDestination.createNavigationRoute(
                                provisionerUuid = provisionerUuid
                            )
                        )
                    }
                )
                networkKeysGraph(
                    onBackPressed = onBackPressed,
                    onNavigateToKey = { netKeyIndex ->
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