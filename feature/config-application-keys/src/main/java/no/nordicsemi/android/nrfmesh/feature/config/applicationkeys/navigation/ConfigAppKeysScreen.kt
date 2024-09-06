package no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import no.nordicsemi.android.nrfmesh.core.navigation.ActionMenuItem
import no.nordicsemi.android.nrfmesh.core.navigation.FloatingActionButton
import no.nordicsemi.android.nrfmesh.core.navigation.Screen

class ConfigAppKeysScreen(
    override val title: String = "Config Application Keys",
) : Screen {
    override val route = ConfigAppKeysDestination.route
    override val showTopBar = true
    override val navigationIcon = null
    override val onNavigationIconClick: (() -> Unit)? = {
        _buttons.tryEmit(Actions.BACK)
    }
    override val actions = emptyList<ActionMenuItem>()
    override val floatingActionButton = listOf(
        FloatingActionButton(
            icon = Icons.Outlined.Add,
            text = "Add Key",
            contentDescription = "Add Key",
            onClick = {
                _buttons.tryEmit(Actions.ADD_KEY)
            }
        )
    )
    override val showBottomBar = true
    private val _buttons = MutableSharedFlow<Actions>(extraBufferCapacity = 1)
    val buttons: SharedFlow<Actions> = _buttons.asSharedFlow()

    enum class Actions {
        ADD_KEY,
        BACK
    }
}