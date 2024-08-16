package no.nordicsemi.android.nrfmesh.feature.ranges.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import no.nordicsemi.android.nrfmesh.core.navigation.ActionMenuItem
import no.nordicsemi.android.nrfmesh.core.navigation.FloatingActionButton
import no.nordicsemi.android.nrfmesh.core.navigation.Screen

class UnicastRangesScreen(
    override val title: String = "Unicast Ranges",
) : RangesScreen(title = title) {
    override val route = UnicastRangesDestination.route
}

class GroupRangesScreen(
    override val title: String = "Group Ranges",
) : RangesScreen(title = title) {
    override val route = GroupRangesDestination.route
}

class SceneRangesScreen(
    override val title: String = "Scene Ranges",
) : RangesScreen(title = title) {
    override val route = UnicastRangesDestination.route
}

abstract class RangesScreen(override val title: String) : Screen {
    override val showTopBar = true
    override val navigationIcon = Icons.AutoMirrored.Outlined.ArrowBack
    override val onNavigationIconClick: (() -> Unit) = {
        _buttons.tryEmit(Actions.BACK)
    }
    override val actions = emptyList<ActionMenuItem>()
    override val floatingActionButton = listOf(
        FloatingActionButton(
            icon = Icons.Outlined.Add,
            text = "Add Range",
            contentDescription = "Add Range",
            onClick = {
                _buttons.tryEmit(Actions.ADD_RANGE)
            }
        )
    )

    private val _buttons = MutableSharedFlow<Actions>(extraBufferCapacity = 1)
    val buttons = _buttons.asSharedFlow()
    override val showBottomBar: Boolean = true

    enum class Actions {
        BACK,
        ADD_RANGE,
        RESOLVE
    }
}