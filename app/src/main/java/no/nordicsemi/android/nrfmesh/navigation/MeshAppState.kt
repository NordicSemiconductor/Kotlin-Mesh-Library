package no.nordicsemi.android.nrfmesh.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material.icons.filled.Hive
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.GroupWork
import androidx.compose.material.icons.outlined.Hive
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.util.trace
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.feature.config.networkkeys.navigation.ConfigNetKeysDestination
import no.nordicsemi.android.feature.config.networkkeys.navigation.ConfigNetKeysScreen
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.core.navigation.R
import no.nordicsemi.android.nrfmesh.core.navigation.TopLevelDestination
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeyDestination
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeyScreen
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeysDestination
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeysScreen
import no.nordicsemi.android.nrfmesh.feature.bind.appkeys.navigation.BoundAppKeysDestination
import no.nordicsemi.android.nrfmesh.feature.bind.appkeys.navigation.BoundAppKeysScreen
import no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.navigation.ConfigAppKeysScreen
import no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.navigation.ConfigAppKeysDestination
import no.nordicsemi.android.nrfmesh.feature.model.navigation.ModelDestination
import no.nordicsemi.android.nrfmesh.feature.model.navigation.ModelScreen
import no.nordicsemi.android.nrfmesh.feature.elements.navigation.ElementDestination
import no.nordicsemi.android.nrfmesh.feature.elements.navigation.ElementScreen
import no.nordicsemi.android.nrfmesh.feature.export.navigation.ExportDestination
import no.nordicsemi.android.nrfmesh.feature.export.navigation.ExportScreen
import no.nordicsemi.android.nrfmesh.feature.groups.navigation.GroupsDestination
import no.nordicsemi.android.nrfmesh.feature.groups.navigation.GroupsScreen
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeyDestination
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeyScreen
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeysDestination
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeysScreen
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.NodeDestination
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.NodeScreen
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.NodesDestination
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.NodesScreen
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.ProvisionerDestination
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.ProvisionerScreen
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.ProvisionersDestination
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.ProvisionersScreen
import no.nordicsemi.android.nrfmesh.feature.provisioning.navigation.NetKeySelectorDestination
import no.nordicsemi.android.nrfmesh.feature.provisioning.navigation.NetKeySelectorScreen
import no.nordicsemi.android.nrfmesh.feature.provisioning.navigation.ProvisioningDestination
import no.nordicsemi.android.nrfmesh.feature.provisioning.navigation.ProvisioningScreen
import no.nordicsemi.android.nrfmesh.feature.proxy.navigation.ProxyDestination
import no.nordicsemi.android.nrfmesh.feature.proxy.navigation.ProxyScreen
import no.nordicsemi.android.nrfmesh.feature.ranges.navigation.GroupRangesDestination
import no.nordicsemi.android.nrfmesh.feature.ranges.navigation.GroupRangesScreen
import no.nordicsemi.android.nrfmesh.feature.ranges.navigation.SceneRangesDestination
import no.nordicsemi.android.nrfmesh.feature.ranges.navigation.SceneRangesScreen
import no.nordicsemi.android.nrfmesh.feature.ranges.navigation.UnicastRangesDestination
import no.nordicsemi.android.nrfmesh.feature.ranges.navigation.UnicastRangesScreen
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.SceneDestination
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.SceneScreen
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.ScenesDestination
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.ScenesScreen
import no.nordicsemi.android.nrfmesh.feature.settings.navigation.SettingsDestination
import no.nordicsemi.android.nrfmesh.feature.settings.navigation.SettingsScreen

@Composable
fun rememberMeshAppState(
    navController: NavHostController,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState
): MeshAppState = remember(navController) {
    MeshAppState(
        navController = navController,
        scope = scope,
        snackbarHostState = snackbarHostState
    )
}

@Stable
class MeshAppState(
    override val navController: NavHostController,
    private val scope: CoroutineScope,
    override val snackbarHostState: SnackbarHostState
) : AppState() {

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
            iconTextId = R.string.label_nav_bar_proxy
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

    /**
     * Navigates to the given destination.
     *
     * @param destination Destination to navigate to.
     * @param route       Route to navigate to.
     */
    internal fun navigate(destination: MeshNavigationDestination, route: String?) {
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
                    // re-selecting the same item
                    launchSingleTop = true
                    // Restore state when re-selecting a previously selected item
                    restoreState = true
                }
            } else {
                navController.navigate(route ?: destination.route)
            }
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
    ProvisionersDestination.route -> ProvisionersScreen(title = "Provisioners")
    ProvisionerDestination.route -> ProvisionerScreen(title = "Edit Provisioner")
    UnicastRangesDestination.route -> UnicastRangesScreen(title = "Unicast Ranges")
    GroupRangesDestination.route -> GroupRangesScreen(title = "Group Ranges")
    SceneRangesDestination.route -> SceneRangesScreen(title = "Scene Ranges")
    NetworkKeysDestination.route -> NetworkKeysScreen(title = "Network Keys")
    NetworkKeyDestination.route -> NetworkKeyScreen(title = "Edit Key")
    ApplicationKeysDestination.route -> ApplicationKeysScreen(title = "Application Keys")
    ApplicationKeyDestination.route -> ApplicationKeyScreen(title = "Edit Key")
    ScenesDestination.route -> ScenesScreen(title = "Scenes")
    SceneDestination.route -> SceneScreen(title = "Edit Scene")
    ExportDestination.route -> ExportScreen(title = "Export")
    else -> null
}