package no.nordicsemi.android.nrfmesh.core.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Stable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * AppState is a class that holds the current state of the application.
 *
 * @property navController          The [NavHostController] that will be used to navigate between
 * @property snackbarHostState      The [SnackbarHostState] that will be used to show snackbars.
 *                                  destinations of the application.
 * @property nodeNavigator          Three Pane Scaffold Navigator used for navigating the node
 *                                  graph.
 * @property groupsNavigator        Three Pane Scaffold Navigator used for navigating the group
 *                                  graph.
 * @property settingsNavigator      Three Pane Scaffold Navigator used for navigating the settings
 *                                  graph.
 *
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Stable
abstract class AppState(
    val navController: NavHostController,
    val snackbarHostState: SnackbarHostState
) {
    var nodeNavigator: ThreePaneScaffoldNavigator<Any>? = null
    var groupsNavigator: ThreePaneScaffoldNavigator<Any>? = null
    var settingsNavigator: ThreePaneScaffoldNavigator<Any>? = null

    val previousBackStackEntry: NavBackStackEntry?
        get() = navController.previousBackStackEntry

    /**
     * Navigate to node.
     *
     * @param uuid UUID of the  node.
     */
    @OptIn(ExperimentalUuidApi::class)
    abstract fun navigateToNode(uuid: Uuid)

    /**
     * Navigate to settings.
     *
     * @param listItem Clickable setting item.
     */
    abstract fun navigateToSettings(listItem: ClickableSetting? = null)

    /**
     * Navigates back.
     */
    abstract fun onBackPressed()
}
