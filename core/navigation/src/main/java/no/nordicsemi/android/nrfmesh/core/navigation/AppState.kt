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
 *                                  destinations of the application.
 * @property currentScreen          Current screen that is displayed.
 *                                  clicked.
 * @property windowSizeClass        The current window size class.
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
