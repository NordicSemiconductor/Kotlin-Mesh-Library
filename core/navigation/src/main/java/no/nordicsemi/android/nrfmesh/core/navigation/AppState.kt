package no.nordicsemi.android.nrfmesh.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * AppState is a class that holds the current state of the application.
 *
 * @property snackbarHostState          The [SnackbarHostState] that will be used to show snackbars.
 * @property topLevelDestinations       A list of [TopLevelDestination] that are the top level
 *                                      destinations of the application.
 * @property currentScreen              Current screen that is displayed.
 * @property showTopAppBar              True if the top app bar should be shown.
 * @property navigationIcon             The navigation icon that should be shown in the top app bar.
 * @property onNavigationIconClick      The action that should be performed when the navigation icon
 *                                      is clicked.
 * @property title                      The title of the current screen.
 * @property actions                    A list of [ActionMenuItem] that should be shown in the top
 *                                      app bar.
 * @property floatingActionButton       A list of [FloatingActionButton] that should be shown in the
 *                                      bottom app bar.
 * @property showBottomBar              True if the bottom app bar should be shown.
 * @property currentScreen              The current screen that is displayed.
 */
@Stable
abstract class AppState {
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

    /**
     * Returns the current screen.
     */
    protected abstract fun currentScreen()

    /**
     * Navigates to the given destination.
     *
     * @param destination The destination to navigate to.
     * @param route       The route to navigate to.
     */
    abstract fun navigate(destination: MeshNavigationDestination, route: String? = null)

    /**
     * Navigates back.
     */
    abstract fun onBackPressed()
}