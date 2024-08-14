package no.nordicsemi.android.feature.config.networkkeys.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import no.nordicsemi.android.nrfmesh.core.navigation.ActionMenuItem
import no.nordicsemi.android.nrfmesh.core.navigation.FloatingActionButton
import no.nordicsemi.android.nrfmesh.core.navigation.Screen

class ConfigNetworkKeysScreen(
    override val title: String = "Config Network Keys",
) : Screen {
    override val route: String
        get() = ConfigNetworkKeyDestination.route
    override val showTopBar: Boolean
        get() = true
    override val navigationIcon: ImageVector?
        get() = null
    override val onNavigationIconClick: (() -> Unit)?
        get() = null
    override val actions: List<ActionMenuItem>
        get() = emptyList()
    override val floatingActionButton: FloatingActionButton
        get() = FloatingActionButton(
            icon = Icons.Outlined.Add,
            text = "Add Key",
            contentDescription = "Add Key",
            onClick = {
                _buttons.tryEmit(Actions.ADD_KEY)
            }
        )
    override val showBottomBar: Boolean
        get() = true
    private val _buttons = MutableSharedFlow<Actions>(extraBufferCapacity = 1)
    val buttons: Flow<Actions> = _buttons.asSharedFlow()

    enum class Actions {
        ADD_KEY,
    }
}