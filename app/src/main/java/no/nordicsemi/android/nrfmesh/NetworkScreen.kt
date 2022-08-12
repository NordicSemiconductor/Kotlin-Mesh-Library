package no.nordicsemi.android.nrfmesh

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.NodesDestination
import no.nordicsemi.android.nrfmesh.navigation.MeshNavHost
import no.nordicsemi.android.nrfmesh.navigation.MeshTopLevelNavigation
import no.nordicsemi.android.nrfmesh.navigation.TOP_LEVEL_DESTINATIONS
import no.nordicsemi.android.nrfmesh.navigation.TopLevelDestination
import no.nordicsemi.android.nrfmesh.viewmodel.NetworkViewModel


@Composable
fun NetworkRoute(
    viewModel: NetworkViewModel = hiltViewModel()
) {
    if (viewModel.isNetworkLoaded)
        NetworkScreen()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NetworkScreen() {
    val navController = rememberNavController()
    val meshTopLevelNavigation = remember(navController) { MeshTopLevelNavigation(navController) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomNavigationBar(
                onNavigateToTopLevelDestination = meshTopLevelNavigation::navigateTo,
                currentDestination = currentDestination
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal
                    )
                )
        ) {
            MeshNavHost(
                modifier = Modifier
                    .consumedWindowInsets(padding),
                navController = navController,
                startDestination = NodesDestination.route,
                snackbarHostState = snackbarHostState
            )
        }
    }
}


@Composable
fun BottomNavigationBar(
    onNavigateToTopLevelDestination: (TopLevelDestination) -> Unit,
    currentDestination: NavDestination?
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        NavigationBar(
            modifier = Modifier.windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                )
            ),
            tonalElevation = 0.dp
        ) {
            TOP_LEVEL_DESTINATIONS.forEach { destination ->
                val selected = currentDestination?.hierarchy?.any {
                    it.route == destination.route
                } == true
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigateToTopLevelDestination(destination) },
                    icon = {
                        Icon(
                            if (selected) {
                                destination.selectedIcon
                            } else {
                                destination.unselectedIcon
                            },
                            contentDescription = null
                        )
                    },
                    label = { Text(stringResource(destination.iconTextId)) }
                )
            }
        }
    }
}

