package no.nordicsemi.android.nrfmesh.feature.nodes.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import no.nordicsemi.android.nrfmesh.core.navigation.ActionMenuItem
import no.nordicsemi.android.nrfmesh.core.navigation.FloatingActionButton
import no.nordicsemi.android.nrfmesh.core.navigation.Screen

class NodesScreen(
    override val title: String = "Nodes"
) : Screen {
    override val route = NodesDestination.route
    override val showTopBar = true
    override val navigationIcon = null
    override val onNavigationIconClick: (() -> Unit)? = null
    override val actions = emptyList<ActionMenuItem>()
    override val floatingActionButton = listOf(
        FloatingActionButton(
            icon = Icons.Outlined.Add,
            text = "Add Node",
            contentDescription = "Add Node",
            onClick = { _buttons.tryEmit(Actions.ADD_NODE) }
        )
    )

    private val _buttons = MutableSharedFlow<Actions>(extraBufferCapacity = 1)
    val buttons = _buttons.asSharedFlow()

    override val showBottomBar = true

    enum class Actions {
        ADD_NODE
    }
}