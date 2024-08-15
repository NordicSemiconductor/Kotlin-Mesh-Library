@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.feature.provisioners.ranges

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.GroupRangesScreen
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
    LaunchedEffect(key1 = screen) {
        screen?.buttons?.onEach { button ->
            when (button) {
                GroupRangesScreen.Actions.BACK -> onBackPressed()
                GroupRangesScreen.Actions.RESOLVE -> resolve()
            }
        }
    }
    RangesScreen(
        uiState = uiState,
        addRange = addRange,
        onRangeUpdated = onRangeUpdated,
        onSwiped = onSwiped,
        onUndoClicked = onUndoClicked,
        remove = remove,
        isValidBound = isValidBound
    )
}