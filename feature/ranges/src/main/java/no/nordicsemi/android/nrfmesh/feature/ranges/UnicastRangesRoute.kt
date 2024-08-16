package no.nordicsemi.android.nrfmesh.feature.ranges

import androidx.compose.runtime.Composable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.kotlin.mesh.core.model.Range

@Composable
internal fun UnicastRangesRoute(
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
    val screen = appState.currentScreen as? no.nordicsemi.android.nrfmesh.feature.ranges.navigation.UnicastRangesScreen
    RangesScreen(
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