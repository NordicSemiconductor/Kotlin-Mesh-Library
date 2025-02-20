package no.nordicsemi.android.nrfmesh.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.nrfmesh.core.navigation.ActionMenuItem
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.FloatingActionButton
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.groups.navigation.GroupRoute
import no.nordicsemi.android.nrfmesh.feature.groups.navigation.GroupsRoute
import no.nordicsemi.android.nrfmesh.feature.groups.navigation.navigateToGroups
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.NodesRoute
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.navigateToNodes
import no.nordicsemi.android.nrfmesh.feature.nodes.node.navigation.NodeRoute
import no.nordicsemi.android.nrfmesh.feature.provisioning.navigation.ProvisioningRoute
import no.nordicsemi.android.nrfmesh.feature.proxy.navigation.ProxyRoute
import no.nordicsemi.android.nrfmesh.feature.proxy.navigation.navigateToProxy
import no.nordicsemi.android.nrfmesh.feature.settings.navigation.SettingsRoute
import no.nordicsemi.android.nrfmesh.feature.settings.navigation.navigateToSettings
import no.nordicsemi.android.nrfmesh.navigation.MeshTopLevelDestination.GROUPS
import no.nordicsemi.android.nrfmesh.navigation.MeshTopLevelDestination.NODES
import no.nordicsemi.android.nrfmesh.navigation.MeshTopLevelDestination.PROXY
import no.nordicsemi.android.nrfmesh.navigation.MeshTopLevelDestination.SETTINGS

@Composable
fun rememberMeshAppState(
    navController: NavHostController,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    windowSizeClass: WindowSizeClass,
): MeshAppState = remember(navController) {
    MeshAppState(
        navController = navController,
        scope = scope,
        snackbarHostState = snackbarHostState,
        windowSizeClass = windowSizeClass
    )
}

@Stable
class MeshAppState(
    navController: NavHostController,
    private val scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    windowSizeClass: WindowSizeClass,
) : AppState(
    navController = navController,
    snackbarHostState = snackbarHostState,
    windowSizeClass = windowSizeClass
) {
    val meshTopLevelDestinations: List<MeshTopLevelDestination> = MeshTopLevelDestination.entries

    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination

    val currentMeshTopLevelDestination: MeshTopLevelDestination?
        @Composable get() = MeshTopLevelDestination.entries.firstOrNull { destination ->
            currentDestination?.hasRoute(route = destination.route) == true
        }

    val showTopAppBar: Boolean
        get() = currentScreen?.showTopBar ?: false

    val navigationIcon: ImageVector
        get() = currentScreen?.navigationIcon ?: Icons.AutoMirrored.Outlined.ArrowBack

    val onNavigationIconClick: (() -> Unit)?
        get() = currentScreen?.onNavigationIconClick

    val title: String
        get() = when {
            navController.currentDestination?.hasRoute<NodesRoute>() == true ->{
              "Nodes"
            }
            navController.currentDestination?.hasRoute<NodeRoute>() == true -> "Node"
            navController.currentDestination?.hasRoute<GroupsRoute>() == true -> "Groups"
            navController.currentDestination?.hasRoute<GroupRoute>() == true -> "Group"
            navController.currentDestination?.hasRoute<ProxyRoute>() == true -> "Proxy"
            navController.currentDestination?.hasRoute<ProvisioningRoute>() == true -> "Provisioning"
            navController.currentDestination?.hasRoute<SettingsRoute>() == true -> "Settings"
            else -> "Unknown"
        }

    val actions: List<ActionMenuItem>
        get() = currentScreen?.actions.orEmpty()

    val floatingActionButton: List<FloatingActionButton>
        get() = currentScreen?.floatingActionButton.orEmpty()

    val showBottomBar: Boolean
        get() = currentScreen?.showBottomBar ?: false

    /**
     * Navigates to the given destination.
     *
     * @param destination Destination to navigate to.
     * @param route       Route to navigate to.
     */
    fun navigate(destination: MeshNavigationDestination, route: String?) {
        navController.navigate(route ?: destination.route) {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // re-selecting the same item
            launchSingleTop = true
            // Restore state when re-selecting a previously selected item
            restoreState = true
        }
        /*if (destination is TopLevelDestination) {
        } else {
            navController.navigate(route ?: destination.route)
        }*/
    }

    fun navigateToTopLevelDestination(destination: MeshTopLevelDestination) {
        val topLevelNavOptions = navOptions {
            // Pop up to the start destination of the graph to avoid building up a large stack of
            // destinations on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when re-selecting the same item
            launchSingleTop = true
            // Restore state when re-selecting a previously selected item
            restoreState = true
        }

        when (destination) {
            NODES -> navController.navigateToNodes(navOptions = topLevelNavOptions)
            GROUPS -> navController.navigateToGroups(navOptions = topLevelNavOptions)
            PROXY -> navController.navigateToProxy(navOptions = topLevelNavOptions)
            SETTINGS -> navController.navigateToSettings(navOptions = topLevelNavOptions)
        }
    }

    fun clearBackStack() {
        navController.popBackStack(navController.graph.findStartDestination().id, false)
        navController.navigateToSettings()
    }

    /**
     * Navigates back.
     */
    internal fun onBackPressed() {
        navController.navigateUp()
    }
}