package no.nordicsemi.android.nrfmesh.feature.settings.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import no.nordicsemi.android.nrfmesh.core.navigation.ActionMenuItem
import no.nordicsemi.android.nrfmesh.core.navigation.FloatingActionButton
import no.nordicsemi.android.nrfmesh.core.navigation.Screen

class SettingsScreen(
    override val title: String = "Settings"
) : Screen {
    override val route = SettingsDestination.route
    override val showTopBar = true
    override val navigationIcon = null
    override val onNavigationIconClick: (() -> Unit)? = null
    override val actions: List<ActionMenuItem> = listOf(
        ActionMenuItem.NeverShown(
            title = "Import",
            onClick = { _buttons.tryEmit(Actions.IMPORT) }
        ),
        ActionMenuItem.NeverShown(
            title = "Export",
            onClick = { _buttons.tryEmit(Actions.EXPORT) }
        ),
        ActionMenuItem.NeverShown(
            title = "Reset",
            onClick = { _buttons.tryEmit(Actions.RESET) }
        ),
    )
    override val floatingActionButton = emptyList<FloatingActionButton>()
    override val showBottomBar = true

    private val _buttons = MutableSharedFlow<Actions>(extraBufferCapacity = 1)
    val buttons = _buttons.asSharedFlow()

    enum class Actions {
        IMPORT,
        EXPORT,
        RESET
    }
}