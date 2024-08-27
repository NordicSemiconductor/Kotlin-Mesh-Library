package no.nordicsemi.android.nrfmesh.feature.ranges

import androidx.compose.runtime.Composable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.feature.ranges.navigation.GroupRangesScreen
import no.nordicsemi.kotlin.mesh.core.model.Range

@Composable
internal fun GroupRangesRoute(
    appState: AppState,
    uiState: RangesScreenUiState,
    addRange: (start: UInt, end: UInt) -> Unit,
    onRangeUpdated: (Range, UShort, UShort) -> Unit,
    onSwiped: (Range) -> Unit,
    onUndoClicked: (Range) -> Unit,
    remove: (Range) -> Unit,
    resolve: () -> Unit,
    isValidBound: (UShort) -> Boolean,
    onBackPressed: () -> Unit
) {
    val screen = appState.currentScreen as? GroupRangesScreen
    RangesScreen(
        snackbarHostState = appState.snackbarHostState,
        screen = screen,
        uiState = uiState,
        addRange = addRange,
        onRangeUpdated = onRangeUpdated,
        onSwiped = onSwiped,
        onUndoClicked = onUndoClicked,
        remove = remove,
        isValidBound = isValidBound,
        resolve = resolve,
        onBackPressed = onBackPressed
    )
}