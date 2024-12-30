package no.nordicsemi.android.nrfmesh.core.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController

/**
 * AppState is a class that holds the current state of the application.
 *
 * @property snackbarHostState      The [SnackbarHostState] that will be used to show snackbars.
 * @property topLevelDestinations   List of [TopLevelDestination] that are the top level
 *                                  destinations of the application.
 * @property currentScreen          Current screen that is displayed.
 * @property showTopAppBar          True if the top app bar should be shown.
 * @property navigationIcon         Navigation icon that should be shown in the top app bar.
 * @property onNavigationIconClick  The action that should be performed when the navigation icon is
 *                                  clicked.
 * @property title                  Title of the current screen.
 * @property actions                List of [ActionMenuItem] that should be shown in the top AppBar.
 * @property floatingActionButton   List of [FloatingActionButton] to be shown in the bottom AppBar.
 * @property showBottomBar          True if the bottom app bar should be shown.
 * @property currentScreen          Current screen that is displayed.
 */
@Stable
abstract class AppState(
    val navController: NavHostController,
    val snackbarHostState: SnackbarHostState,
    val windowSizeClass: WindowSizeClass
) {
    var currentScreen by mutableStateOf<Screen?>(null)
        protected set

    val previousBackStackEntry: NavBackStackEntry?
        get() = navController.previousBackStackEntry
}