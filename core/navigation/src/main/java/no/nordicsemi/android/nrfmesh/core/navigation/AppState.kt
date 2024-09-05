package no.nordicsemi.android.nrfmesh.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

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
abstract class AppState {
    protected abstract val navController: NavController

    abstract val snackbarHostState: SnackbarHostState

    abstract val topLevelDestinations: List<TopLevelDestination>

    var currentScreen by mutableStateOf<Screen?>(null)
        protected set

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

    val previousBackStackEntry: NavBackStackEntry?
        get() = navController.previousBackStackEntry

    /**
     * Returns the current screen.
     */
    protected abstract fun currentScreen()
}