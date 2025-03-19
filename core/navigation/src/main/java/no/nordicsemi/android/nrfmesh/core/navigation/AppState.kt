package no.nordicsemi.android.nrfmesh.core.navigation

import androidx.compose.material3.SnackbarHostState
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
 */
@Stable
abstract class AppState(
    val navController: NavHostController,
    val snackbarHostState: SnackbarHostState,
    val windowSizeClass: WindowSizeClass,
) {
    val previousBackStackEntry: NavBackStackEntry?
        get() = navController.previousBackStackEntry

    abstract fun navigateToNode(uuid: UUID)

    /**
     * Navigates back.
     */
    abstract fun onBackPressed()
}
