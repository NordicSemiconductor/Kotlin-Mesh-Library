package no.nordicsemi.android.nrfmesh.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.feature.groups.navigation.groupsGraph
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.NodesBaseRoute
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.nodesGraph
import no.nordicsemi.android.nrfmesh.feature.provisioning.navigation.provisioningGraph
import no.nordicsemi.android.nrfmesh.feature.proxy.navigation.proxyFilterGraph
import no.nordicsemi.android.nrfmesh.feature.settings.navigation.settingsListDetailsScreen

@Composable
fun MeshNavHost(
    appState: AppState,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit
) {
    NavHost(
        modifier = modifier,
        navController = appState.navController,
        startDestination = NodesBaseRoute
    ) {
        nodesGraph(appState = appState, navigateBack = onBackPressed)
        groupsGraph(appState = appState)
        proxyFilterGraph()
        settingsListDetailsScreen()
        provisioningGraph(appState = appState, onBackPressed = onBackPressed)
    }
}