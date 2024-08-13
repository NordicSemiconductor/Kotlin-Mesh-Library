package no.nordicsemi.android.nrfmesh.navigation

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material.icons.filled.Hive
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.GroupWork
import androidx.compose.material.icons.outlined.Hive
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.util.trace
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.core.navigation.R
import no.nordicsemi.android.nrfmesh.core.navigation.Screen
import no.nordicsemi.android.nrfmesh.core.navigation.TopLevelDestination
import no.nordicsemi.android.nrfmesh.feature.groups.navigation.GroupsDestination
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.NodesScreen
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.NodesDestination
import no.nordicsemi.android.nrfmesh.feature.proxy.navigation.ProxyDestination
import no.nordicsemi.android.nrfmesh.feature.settings.navigation.SettingsDestination
import no.nordicsemi.android.nrfmesh.feature.settings.navigation.SettingsScreen

@Composable
fun rememberMeshAppState(
    navController: NavHostController = rememberNavController(),
    scope: CoroutineScope
): MeshAppState {
    return remember(navController) {
        MeshAppState(
            navController = navController,
            scope = scope
        )
    }
}

@Stable
class MeshAppState(
    val navController: NavHostController,
    private val scope: CoroutineScope
) : AppState() {

    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination

    override val topLevelDestinations = listOf(
        TopLevelDestination(
            route = NodesDestination.route,
            destination = NodesDestination.destination,
            selectedIcon = Icons.Filled.Hive,
            unselectedIcon = Icons.Outlined.Hive,
            iconTextId = R.string.label_nav_bar_nodes
        ),
        TopLevelDestination(
            route = GroupsDestination.route,
            destination = GroupsDestination.destination,
            selectedIcon = Icons.Filled.GroupWork,
            unselectedIcon = Icons.Outlined.GroupWork,
            iconTextId = R.string.label_nav_bar_groups
        ),
        TopLevelDestination(
            route = ProxyDestination.route,
            destination = ProxyDestination.destination,
            selectedIcon = Icons.Filled.Hub,
            unselectedIcon = Icons.Outlined.Hub,
            iconTextId = R.string.label_nav_bar_proxy_filter
        ),
        TopLevelDestination(
            route = SettingsDestination.route,
            destination = SettingsDestination.destination,
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings,
            iconTextId = R.string.label_nav_bar_settings
        )
    )

    init {
        currentScreen()
    }

    override fun currentScreen() {
        navController.currentBackStackEntryFlow
            .distinctUntilChanged()
            .onEach { backStackEntry ->
                val route = backStackEntry.destination.route
                currentScreen = getScreen(route)
            }
            .launchIn(scope)
    }

    override fun navigate(destination: MeshNavigationDestination, route: String?) {
        trace("Navigation: $destination") {
            if (destination is TopLevelDestination) {
                navController.navigate(route ?: destination.route) {
                    // Pop up to the start destination of the graph to
                    // avoid building up a large stack of destinations
                    // on the back stack as users select items
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    // Avoid multiple copies of the same destination when
                    // reselecting the same item
                    launchSingleTop = true
                    // Restore state when reselecting a previously selected item
                    restoreState = true
                }
            } else {
                navController.navigate(route ?: destination.route)
            }
        }
    }

    override fun onBackPressed() {
        navController.popBackStack()
    }
}


fun getScreen(route: String?): Screen? {
    Log.d("AAA", "getScreen: $route")
    return when (route) {
        NodesDestination.route -> NodesScreen(title = "Nodes")
        GroupsDestination.route -> NodesScreen(title = "Groups")
        ProxyDestination.route -> NodesScreen(title = "Proxy")
        SettingsDestination.route -> SettingsScreen(title = "Settings")
        else -> null
    }
}