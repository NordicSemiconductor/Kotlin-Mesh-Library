package no.nordicsemi.android.feature.config.networkkeys.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import no.nordicsemi.android.nrfmesh.core.navigation.ActionMenuItem
import no.nordicsemi.android.nrfmesh.core.navigation.FloatingActionButton
import no.nordicsemi.android.nrfmesh.core.navigation.Screen

class ConfigNetworkKeysScreen(
    override val title: String = "Config Network Keys",
) : Screen {
    override val route = ConfigNetworkKeyDestination.route
    override val showTopBar = true
    override val navigationIcon = null
    override val onNavigationIconClick: (() -> Unit)? = null
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
    }
}