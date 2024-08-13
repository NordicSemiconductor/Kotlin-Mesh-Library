package no.nordicsemi.android.nrfmesh.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.applicationKeysGraph
import no.nordicsemi.android.nrfmesh.feature.export.navigation.exportGraph
import no.nordicsemi.android.nrfmesh.feature.groups.navigation.groupsGraph
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.networkKeysGraph
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.NodesDestination
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.nodesGraph
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.provisionersGraph
import no.nordicsemi.android.nrfmesh.feature.proxy.navigation.proxyFilterGraph
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.scenesGraph
import no.nordicsemi.android.nrfmesh.feature.settings.navigation.settingsGraph


@Composable
fun MeshNavHost(
    appState: AppState,
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
            appState = appState,
            onNavigateToDestination = onNavigateToDestination,
            onBackPressed = onBackPressed,
        )
        groupsGraph(
            appState = appState,
            onNavigateToDestination = onNavigateToDestination,
            onBackPressed = onBackPressed,
        )
        proxyFilterGraph()
        settingsGraph(
            onNavigateToDestination = onNavigateToDestination,
            onBackPressed = onBackPressed
        )
    }
}