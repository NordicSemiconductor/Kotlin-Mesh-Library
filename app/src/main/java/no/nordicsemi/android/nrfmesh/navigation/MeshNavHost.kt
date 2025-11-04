package no.nordicsemi.android.nrfmesh.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.feature.groups.navigation.groupsGraph
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.NodesBaseRoute
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.nodesGraph
import no.nordicsemi.android.nrfmesh.feature.nodes.node.navigation.nodeGraph
import no.nordicsemi.android.nrfmesh.feature.provisioning.navigation.provisioningGraph
import no.nordicsemi.android.nrfmesh.feature.proxy.navigation.proxyFilterGraph
import no.nordicsemi.android.nrfmesh.feature.settings.navigation.settingsListDetailsScreen

@Composable
fun MeshNavHost(appState: AppState, modifier: Modifier = Modifier, onBackPressed: () -> Unit) {
    NavHost(
        modifier = modifier,
        navController = appState.navController,
        startDestination = NodesBaseRoute
    ) {
        nodesGraph(
            appState = appState,
            nodeGraph = {
                nodeGraph(appState = appState, navigateBack = onBackPressed)
            },
            provisioningGraph = {
                provisioningGraph(appState = appState, onBackPressed = onBackPressed)
            }
        )
        groupsGraph(appState = appState)
        proxyFilterGraph()
        settingsListDetailsScreen(appState = appState, onBackPressed = onBackPressed)
    }
}