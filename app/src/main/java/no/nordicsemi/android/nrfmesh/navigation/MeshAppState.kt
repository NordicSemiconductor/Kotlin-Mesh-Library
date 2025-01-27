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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.feature.config.networkkeys.navigation.ConfigNetKeysDestination
import no.nordicsemi.android.feature.config.networkkeys.navigation.ConfigNetKeysScreen
import no.nordicsemi.android.nrfmesh.core.navigation.ActionMenuItem
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.FloatingActionButton
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.bind.appkeys.navigation.BoundAppKeysDestination
import no.nordicsemi.android.nrfmesh.feature.bind.appkeys.navigation.BoundAppKeysScreen
import no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.navigation.ConfigAppKeysDestination
import no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.navigation.ConfigAppKeysScreen
import no.nordicsemi.android.nrfmesh.feature.elements.navigation.ElementDestination
import no.nordicsemi.android.nrfmesh.feature.elements.navigation.ElementScreen
import no.nordicsemi.android.nrfmesh.feature.export.navigation.ExportDestination
import no.nordicsemi.android.nrfmesh.feature.export.navigation.ExportScreen
import no.nordicsemi.android.nrfmesh.feature.groups.navigation.GROUPS_ROUTE
import no.nordicsemi.android.nrfmesh.feature.groups.navigation.GroupsDestination
import no.nordicsemi.android.nrfmesh.feature.groups.navigation.GroupsScreen
import no.nordicsemi.android.nrfmesh.feature.groups.navigation.navigateToGroups
import no.nordicsemi.android.nrfmesh.feature.model.navigation.ModelDestination
import no.nordicsemi.android.nrfmesh.feature.model.navigation.ModelScreen
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.NODES_ROUTE
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.NodeDestination
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.NodeScreen
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.NodesDestination
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.NodesScreen
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.navigateToNodes
import no.nordicsemi.android.nrfmesh.feature.provisioning.navigation.NetKeySelectorDestination
import no.nordicsemi.android.nrfmesh.feature.provisioning.navigation.NetKeySelectorScreen
import no.nordicsemi.android.nrfmesh.feature.provisioning.navigation.ProvisioningDestination
import no.nordicsemi.android.nrfmesh.feature.provisioning.navigation.ProvisioningScreen
import no.nordicsemi.android.nrfmesh.feature.proxy.navigation.PROXY_ROUTE
import no.nordicsemi.android.nrfmesh.feature.proxy.navigation.ProxyDestination
import no.nordicsemi.android.nrfmesh.feature.proxy.navigation.ProxyScreen
import no.nordicsemi.android.nrfmesh.feature.proxy.navigation.navigateToProxy
import no.nordicsemi.android.nrfmesh.feature.settings.navigation.SETTINGS_ROUTE
import no.nordicsemi.android.nrfmesh.feature.settings.navigation.SettingsDestination
import no.nordicsemi.android.nrfmesh.feature.settings.navigation.SettingsScreen
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
    windowSizeClass: WindowSizeClass
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
    windowSizeClass: WindowSizeClass
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
        @Composable get() = when (currentDestination?.route) {
            NODES_ROUTE -> NODES
            GROUPS_ROUTE -> GROUPS
            PROXY_ROUTE -> PROXY
            SETTINGS_ROUTE -> SETTINGS
            else -> null
        }

    val showTopAppBar: Boolean
        get() = currentScreen?.showTopBar ?: false

    val navigationIcon: ImageVector
        get() = currentScreen?.navigationIcon ?: Icons.AutoMirrored.Outlined.ArrowBack

    val onNavigationIconClick: (() -> Unit)?
        get() = currentScreen?.onNavigationIconClick

    val title: String
        get() = currentScreen?.title.orEmpty()

    val actions: List<ActionMenuItem>
        get() = currentScreen?.actions.orEmpty()

    val floatingActionButton: List<FloatingActionButton>
        get() = currentScreen?.floatingActionButton.orEmpty()

    val showBottomBar: Boolean
        get() = currentScreen?.showBottomBar ?: false

    init {
        currentScreen()
    }

    private fun currentScreen() {
        navController.currentBackStackEntryFlow
            .distinctUntilChanged()
            .onEach { backStackEntry ->
                val route = backStackEntry.destination.route
                currentScreen = getScreen(route)
            }
            .launchIn(scope)
    }

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

    /**
     * Navigates back.
     */
    internal fun onBackPressed() {
        navController.navigateUp()
    }
}

private fun getScreen(route: String?) = when (route) {
    NodesDestination.route -> NodesScreen(title = "Nodes")
    NodeDestination.route -> NodeScreen(title = "Node")
    ElementDestination.route -> ElementScreen(title = "Element Information")
    ModelDestination.route -> ModelScreen(title = "Model Information")
    BoundAppKeysDestination.route -> BoundAppKeysScreen()
    ProvisioningDestination.route -> ProvisioningScreen(title = "Provisioning")
    NetKeySelectorDestination.route -> NetKeySelectorScreen()
    ConfigNetKeysDestination.route -> ConfigNetKeysScreen()
    ConfigAppKeysDestination.route -> ConfigAppKeysScreen()
    GroupsDestination.route -> GroupsScreen(title = "Groups")
    ProxyDestination.route -> ProxyScreen(title = "Proxy")
    SettingsDestination.route -> SettingsScreen(title = "Settings")
    ExportDestination.route -> ExportScreen(title = "Export")
    else -> null
}