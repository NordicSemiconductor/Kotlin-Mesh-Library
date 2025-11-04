package no.nordicsemi.android.nrfmesh.core.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Stable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import java.util.UUID

/**
 * AppState is a class that holds the current state of the application.
 *
 * @property navController          The [NavHostController] that will be used to navigate between
 * @property snackbarHostState      The [SnackbarHostState] that will be used to show snackbars.
 *                                  destinations of the application.
 * @property windowSizeClass        The current window size class.
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
    val snackbarHostState: SnackbarHostState,
    val windowSizeClass: WindowSizeClass,
    val nodeNavigator: ThreePaneScaffoldNavigator<Any>,
    val groupsNavigator: ThreePaneScaffoldNavigator<Any>,
    val settingsNavigator: ThreePaneScaffoldNavigator<Any>
) {

    val previousBackStackEntry: NavBackStackEntry?
        get() = navController.previousBackStackEntry

    /**
     * Navigate to node.
     *
     * @param uuid UUID of the  node.
     */
    abstract fun navigateToNode(uuid: UUID)

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
