package no.nordicsemi.android.nrfmesh.feature.provisioners.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import no.nordicsemi.android.nrfmesh.core.navigation.ActionMenuItem
import no.nordicsemi.android.nrfmesh.core.navigation.FloatingActionButton
import no.nordicsemi.android.nrfmesh.core.navigation.Screen

class UnicastRangesScreen(
    override val title: String = "Unicast Ranges",
) : RangesScreen(title = title) {
    override val route: String = UnicastRangesDestination.route
}

class GroupRangesScreen(
    override val title: String = "Group Ranges",
) : RangesScreen(title = title) {
    override val route: String = GroupRangesDestination.route
}

class SceneRangesScreen(
    override val title: String = "Scene Ranges",
) : RangesScreen(title = title) {
    override val route: String = UnicastRangesDestination.route
}

abstract class RangesScreen(override val title: String) : Screen {
    override val showTopBar: Boolean = true
    override val navigationIcon: ImageVector = Icons.AutoMirrored.Outlined.ArrowBack
    override val onNavigationIconClick: (() -> Unit) = {
        _buttons.tryEmit(Actions.BACK)
    }
    override val actions: List<ActionMenuItem> = emptyList()
    override val floatingActionButton: FloatingActionButton? = FloatingActionButton(
        icon = Icons.Outlined.Add,
        text = "Add Range",
        contentDescription = "Add Range",
        onClick = { _buttons.tryEmit(Actions.FAB_ACTION) }
    )

    protected val _buttons = MutableSharedFlow<Actions>(extraBufferCapacity = 1)
    val buttons: Flow<Actions> = _buttons.asSharedFlow()
    override val showBottomBar: Boolean = true

    enum class Actions {
        BACK,
        FAB_ACTION
    }
}