package no.nordicsemi.android.nrfmesh.feature.nodes.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import no.nordicsemi.android.nrfmesh.core.navigation.ActionMenuItem
import no.nordicsemi.android.nrfmesh.core.navigation.FloatingActionButton
import no.nordicsemi.android.nrfmesh.core.navigation.Screen

class NodeScreen(override val title: String = "Node") : Screen {
    override val route = NodeDestination.route
    override val showTopBar = true
    override val navigationIcon = Icons.AutoMirrored.Outlined.ArrowBack
    override val onNavigationIconClick: (() -> Unit) = {
        _buttons.tryEmit(Actions.BACK)
    }
    override val actions = emptyList<ActionMenuItem>()
    override val floatingActionButton = emptyList<FloatingActionButton>()
    override val showBottomBar = true

    private val _buttons = MutableSharedFlow<Actions>(extraBufferCapacity = 1)
    val buttons = _buttons.asSharedFlow()

    enum class Actions {
        BACK
    }
}