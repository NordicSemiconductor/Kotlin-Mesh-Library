package no.nordicsemi.android.nrfmesh.core.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.Stable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController

/**
 * AppState is a class that holds the current state of the application.
 *
 * @property snackbarHostState      The [SnackbarHostState] that will be used to show snackbars.
 * @property settingsNavigator      Three Pane Scaffold Navigator used for navigating the settings
 *                                  graph.
 *
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Stable
abstract class AppState(
    val navigationState: NavigationState,
    val snackbarHostState: SnackbarHostState,
) {
    var settingsNavigator: ThreePaneScaffoldNavigator<Any>? = null
}
