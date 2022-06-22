package no.nordicsemi.android.nrfmesh

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import no.nordicsemi.android.nrfmesh.feature.groups.navigation.GroupsDestination
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.NodesDestination
import no.nordicsemi.android.nrfmesh.feature.proxyfilter.navigation.ProxyFilterDestination
import no.nordicsemi.android.nrfmesh.feature.settings.navigation.SettingsDestination
import no.nordicsemi.android.nrfmesh.navigation.MeshTopLevelNavigation
import no.nordicsemi.android.nrfmesh.navigation.TOP_LEVEL_DESTINATIONS
import no.nordicsemi.android.nrfmesh.navigation.TopLevelDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onCancelled: () -> Unit = {},
) {
    val navController = rememberNavController()
    val meshTopLevelNavigation = remember(navController) {
        MeshTopLevelNavigation(navController)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        topBar = {
            SmallTopAppBar(title = { Text(text = stringResource(R.string.label_network)) })
        },
        bottomBar = {
            BottomNavigationBar(
                onNavigateToTopLevelDestination = meshTopLevelNavigation::navigateTo,
                currentDestination = currentDestination
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
        ) {
            NavHost(
                navController = navController,
                startDestination = NodesDestination.route
            ) {
                composable(NodesDestination.route) {

                }
                composable(GroupsDestination.route) {

                }
                composable(ProxyFilterDestination.route) {

                }
                composable(SettingsDestination.route) {

                }
            }
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
                val selected =
                    currentDestination?.hierarchy?.any { it.route == destination.route } == true
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
