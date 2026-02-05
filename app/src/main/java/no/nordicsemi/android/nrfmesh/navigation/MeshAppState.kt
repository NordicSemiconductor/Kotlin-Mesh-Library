package no.nordicsemi.android.nrfmesh.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.NavigationState

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun rememberMeshAppState(
    snackbarHostState: SnackbarHostState,
    navigationState: NavigationState,
): MeshAppState = remember(navigationState) {
    MeshAppState(
        navigationState = navigationState,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Stable
class MeshAppState(
    navigationState: NavigationState,
    snackbarHostState: SnackbarHostState,
) : AppState(
    navigationState = navigationState,
    snackbarHostState = snackbarHostState
) {
    val showBackButton: Boolean
        get() = !navigationState.topLevelKeys.contains(navigationState.currentKey)
}