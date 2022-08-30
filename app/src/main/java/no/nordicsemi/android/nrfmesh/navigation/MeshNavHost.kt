package no.nordicsemi.android.nrfmesh.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
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
            navigateToProvisioners = {
                // onNavigateToDestination(NetworkKeysDestination, NetworkKeysDestination.route)
            },
            navigateToNetworkKeys = {
                onNavigateToDestination(NetworkKeysDestination, NetworkKeysDestination.route)
            },
            navigateToApplicationKeys = {
                // onNavigateToDestination(NetworkKeysDestination, NetworkKeysDestination.route)
            },
            navigateToScenes = {
                // onNavigateToDestination(NetworkKeysDestination, NetworkKeysDestination.route)
            },
            navigateToExportNetwork = {
                onNavigateToDestination(ExportDestination, ExportDestination.route)
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
            }
        )
    }
}