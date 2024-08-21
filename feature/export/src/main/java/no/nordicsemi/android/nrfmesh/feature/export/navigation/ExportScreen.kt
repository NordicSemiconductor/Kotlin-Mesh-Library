package no.nordicsemi.android.nrfmesh.feature.export.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import no.nordicsemi.android.nrfmesh.core.navigation.ActionMenuItem
import no.nordicsemi.android.nrfmesh.core.navigation.FloatingActionButton
import no.nordicsemi.android.nrfmesh.core.navigation.Screen

class ExportScreen(override val title: String) : Screen {
    override val route = ExportDestination.route
    override val showTopBar = true
    override val navigationIcon = null
    override val onNavigationIconClick: (() -> Unit) = {
        _buttons.tryEmit(Actions.BACK)
    }
    override val actions = listOf(
        ActionMenuItem.IconMenuItem.AlwaysShown(
            title = "Save",
            icon = Icons.Outlined.Save,
            contentDescription = "Save",
            onClick = { _buttons.tryEmit(Actions.SAVE) }
        )
    )
    override val floatingActionButton = emptyList<FloatingActionButton>()
    override val showBottomBar = true

    private val _buttons = MutableSharedFlow<Actions>(extraBufferCapacity = 1)
    val buttons = _buttons.asSharedFlow()

    enum class Actions {
        SAVE,
        BACK
    }
}