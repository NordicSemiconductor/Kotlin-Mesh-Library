package no.nordicsemi.android.nrfmesh.core.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Stable

/**
 * AppState is a class that holds the current state of the application.
 *
 * @property snackbarHostState   [SnackbarHostState] that will be used to show snackbars.
 * @property navigationState     [NavigationState] that will be used to navigate between screens.
 *
 */
@Stable
abstract class AppState(
    val navigationState: NavigationState,
    val snackbarHostState: SnackbarHostState,
)
