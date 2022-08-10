package no.nordicsemi.android.nrfmesh.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import no.nordicsemi.android.nrfmesh.feature.export.navigation.ExportDestination
import no.nordicsemi.android.nrfmesh.feature.export.navigation.exportGraph
import no.nordicsemi.android.nrfmesh.feature.groups.navigation.groupsGraph
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.NodesDestination
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.nodesGraph
import no.nordicsemi.android.nrfmesh.feature.proxyfilter.navigation.proxyFilterGraph
import no.nordicsemi.android.nrfmesh.feature.settings.navigation.settingsGraph

@Composable
fun MeshNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = NodesDestination.route,
    snackbarHostState: SnackbarHostState
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
                navController.navigate("${ExportDestination.route}/$it")
            },
            navigateToNetworkKeys = {
                navController.navigate("${ExportDestination.route}/$it")
            },
            navigateToApplicationKeys = {
                navController.navigate("${ExportDestination.route}/$it")
            },
            navigateToScenes = {
                navController.navigate("${ExportDestination.route}/$it")
            },
            navigateToIvIndex = {
                navController.navigate("${ExportDestination.route}/$it")
            },
            nestedGraphs = {
                exportGraph(snackbarHostState)
            }
        )
        //exportGraph()
    }
}