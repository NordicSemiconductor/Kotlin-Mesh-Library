package no.nordicsemi.android.nrfmesh.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.groups.navigation.groupsGraph
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.NodesDestination
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.nodesGraph
import no.nordicsemi.android.nrfmesh.feature.proxy.navigation.proxyFilterGraph
import no.nordicsemi.android.nrfmesh.feature.settings.navigation.settingsListDetailsScreen


@Composable
fun MeshNavHost(
    appState: AppState,
    onNavigateToDestination: (MeshNavigationDestination, String) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    startDestination: String = NodesDestination.route
) {
    NavHost(
        modifier = modifier,
        navController = appState.navController,
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
        settingsListDetailsScreen(appState = appState)
        /*settingsGraph(
            appState = appState,
            onNavigateToDestination = onNavigateToDestination,
            onBackPressed = onBackPressed
        )*/
    }
}