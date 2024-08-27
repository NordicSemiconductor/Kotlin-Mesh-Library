package no.nordicsemi.android.nrfmesh.feature.provisioning.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import no.nordicsemi.android.nrfmesh.core.navigation.ActionMenuItem
import no.nordicsemi.android.nrfmesh.core.navigation.FloatingActionButton
import no.nordicsemi.android.nrfmesh.core.navigation.Screen

class NetKeySelectorScreen(
    override val title: String = "Select Network Key",
) : Screen {
    override val route = NetKeySelectorDestination.route
    override val showTopBar = true
    override val navigationIcon = Icons.AutoMirrored.Outlined.ArrowBack
    override val onNavigationIconClick: (() -> Unit) = {
        _buttons.tryEmit(Actions.BACK)
    }
    override val actions = emptyList<ActionMenuItem>()
    override val floatingActionButton  = emptyList<FloatingActionButton>()
    override val showBottomBar = false

    private val _buttons = MutableSharedFlow<Actions>(extraBufferCapacity = 1)
    val buttons = _buttons.asSharedFlow()

    enum class Actions {
        BACK
    }
}