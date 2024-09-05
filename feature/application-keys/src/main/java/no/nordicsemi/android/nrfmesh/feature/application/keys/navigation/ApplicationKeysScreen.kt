package no.nordicsemi.android.nrfmesh.feature.application.keys.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import no.nordicsemi.android.nrfmesh.core.navigation.ActionMenuItem
import no.nordicsemi.android.nrfmesh.core.navigation.FloatingActionButton
import no.nordicsemi.android.nrfmesh.core.navigation.Screen

class ApplicationKeysScreen(
    override val title: String = "Network Keys"
) : Screen {
    override val route = ApplicationKeysDestination.route
    override val showTopBar = true
    override val navigationIcon = null
    override val onNavigationIconClick: (() -> Unit) = {
        _buttons.tryEmit(Actions.BACK)
    }
    override val actions = emptyList<ActionMenuItem>()
    override val floatingActionButton = listOf(
        FloatingActionButton(
            icon = Icons.Outlined.Add,
            text = "Add Key",
            contentDescription = "Add Key",
            onClick = { _buttons.tryEmit(Actions.ADD_KEY) }
        )
    )

    private val _buttons = MutableSharedFlow<Actions>(extraBufferCapacity = 1)
    val buttons = _buttons.asSharedFlow()

    override val showBottomBar = true

    enum class Actions {
        ADD_KEY,
        BACK
    }
}