package no.nordicsemi.android.nrfmesh.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.ClickableSetting
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeyContent
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeysContent
import no.nordicsemi.android.nrfmesh.feature.groups.navigation.GroupRoute
import no.nordicsemi.android.nrfmesh.feature.groups.navigation.GroupsRoute
import no.nordicsemi.android.nrfmesh.feature.groups.navigation.navigateToGroups
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeyContent
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeysContent
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.NodesRoute
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.navigateToNodes
import no.nordicsemi.android.nrfmesh.feature.nodes.node.navigation.NodeRoute
import no.nordicsemi.android.nrfmesh.feature.nodes.node.navigation.navigateToNode
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.ProvisionerContent
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.ProvisionersContent
import no.nordicsemi.android.nrfmesh.feature.provisioning.navigation.ProvisioningRoute
import no.nordicsemi.android.nrfmesh.feature.proxy.navigation.ProxyRoute
import no.nordicsemi.android.nrfmesh.feature.proxy.navigation.navigateToProxy
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.SceneContent
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.ScenesContent
import no.nordicsemi.android.nrfmesh.feature.settings.navigation.SettingsRoute
import no.nordicsemi.android.nrfmesh.feature.settings.navigation.navigateToSettings
import no.nordicsemi.android.nrfmesh.navigation.MeshTopLevelDestination.GROUPS
import no.nordicsemi.android.nrfmesh.navigation.MeshTopLevelDestination.NODES
import no.nordicsemi.android.nrfmesh.navigation.MeshTopLevelDestination.PROXY
import no.nordicsemi.android.nrfmesh.navigation.MeshTopLevelDestination.SETTINGS
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun rememberMeshAppState(
    scope: CoroutineScope = rememberCoroutineScope(),
    navController: NavHostController = rememberNavController(),
    snackbarHostState: SnackbarHostState
): MeshAppState = remember(navController) {
    MeshAppState(
        scope = scope,
        navController = navController,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Stable
class MeshAppState(
    val scope: CoroutineScope,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState
) : AppState(
    navController = navController,
    snackbarHostState = snackbarHostState
) {
    val meshTopLevelDestinations: List<MeshTopLevelDestination> = MeshTopLevelDestination.entries

    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination

    val currentMeshTopLevelDestination: MeshTopLevelDestination?
        @Composable get() = MeshTopLevelDestination.entries.firstOrNull { destination ->
            currentDestination?.hasRoute(route = destination.route) == true
        }

    val showBackButton: Boolean
        get() = when {
            navController.currentDestination?.hasRoute<NodeRoute>() == true ||
                    navController.currentDestination?.hasRoute<GroupRoute>() == true ||
                    navController.currentDestination?.hasRoute<ProvisioningRoute>() == true -> true
            // Check against the Settings navigator
            settingsNavigator?.currentDestination?.contentKey is ScenesContent ||
                    settingsNavigator?.currentDestination?.contentKey is SceneContent ||
                    settingsNavigator?.currentDestination?.contentKey is ProvisionersContent ||
                    settingsNavigator?.currentDestination?.contentKey is ProvisionerContent ||
                    settingsNavigator?.currentDestination?.contentKey is NetworkKeysContent ||
                    settingsNavigator?.currentDestination?.contentKey is NetworkKeyContent ||
                    settingsNavigator?.currentDestination?.contentKey is ApplicationKeysContent ||
                    settingsNavigator?.currentDestination?.contentKey is ApplicationKeyContent -> true

            else -> false
        }

    val title: String
        get() = when {
            navController.currentDestination?.hasRoute<NodesRoute>() == true -> "Nodes"
            navController.currentDestination?.hasRoute<NodeRoute>() == true -> "Node"
            navController.currentDestination?.hasRoute<GroupsRoute>() == true -> "Groups"
            navController.currentDestination?.hasRoute<GroupRoute>() == true -> "Group"
            navController.currentDestination?.hasRoute<ProxyRoute>() == true -> "Proxy"
            navController.currentDestination?.hasRoute<SettingsRoute>() == true -> "Settings"
            navController.currentDestination?.hasRoute<ProvisioningRoute>() == true -> "Add Node"
            else -> "Unknown"
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

    internal fun clearBackStack() {
        navController.popBackStack(navController.graph.findStartDestination().id, false)
        navController.navigateToSettings()
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun navigateToNode(uuid: Uuid) {
        navController.navigateToNode(
            uuid = uuid,
            navOptions = navOptions {
                popUpTo(route = ProvisioningRoute) {
                    inclusive = true
                }
            }
        )
    }

    override fun navigateToSettings(listItem: ClickableSetting?) {
        navController.navigateToSettings(listItem = listItem)
    }

    override fun onBackPressed() {
        navController.graph.startDestinationRoute?.let { route ->
            when {
                route.contains("node", ignoreCase = true) -> {
                    scope.launch {
                        if (nodeNavigator?.canNavigateBack() == true)
                            nodeNavigator?.navigateBack()
                        else navController.navigateUp()
                    }
                }

                route.contains("group", ignoreCase = true) -> {
                    scope.launch {
                        if (groupsNavigator?.canNavigateBack() == true)
                            groupsNavigator?.navigateBack()
                        else navController.navigateUp()
                    }
                }

                route.contains("settings", ignoreCase = true) -> {
                    scope.launch {
                        if (settingsNavigator?.canNavigateBack() == true)
                            settingsNavigator?.navigateBack()
                        else navController.navigateUp()
                    }
                }

                else -> navController.navigateUp()
            }
        } ?: navController.navigateUp()
    }
}