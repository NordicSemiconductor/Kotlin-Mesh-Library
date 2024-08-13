package no.nordicsemi.android.nrfmesh.feature.settings.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import no.nordicsemi.android.nrfmesh.core.navigation.ActionItem
import no.nordicsemi.android.nrfmesh.core.navigation.ActionMenuItem
import no.nordicsemi.android.nrfmesh.core.navigation.FloatingActionButton
import no.nordicsemi.android.nrfmesh.core.navigation.Screen

class SettingsScreen(
    override val title: String = "Settings"
) : Screen {
    override val route: String
        get() = SettingsDestination.route
    override val showTopBar: Boolean
        get() = true
    override val navigationIcon: ImageVector?
        get() = null
    override val onNavigationIconClick: (() -> Unit)?
        get() = null
    override val actions: List<ActionMenuItem>
        get() = listOf(
            ActionMenuItem.NeverShown(
                title = "Import",
                onClick = { _buttons.tryEmit(AppBarActions.IMPORT) }
            ),
            ActionMenuItem.NeverShown(
                title = "Export",
                onClick = { _buttons.tryEmit(AppBarActions.EXPORT) }
            ),
            ActionMenuItem.NeverShown(
                title = "Reset",
                onClick = { _buttons.tryEmit(AppBarActions.RESET) }
            ),
        )
    override val floatingActionButton: FloatingActionButton?
        get() = null
    override val showBottomBar: Boolean
        get() = true

    private val _buttons = MutableSharedFlow<AppBarActions>(extraBufferCapacity = 1)
    val buttons: Flow<AppBarActions> = _buttons.asSharedFlow()

    enum class AppBarActions {
        IMPORT,
        EXPORT,
        RESET
    }
}