package no.nordicsemi.android.nrfmesh.feature.bind.appkeys.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import no.nordicsemi.android.nrfmesh.core.navigation.ActionMenuItem
import no.nordicsemi.android.nrfmesh.core.navigation.FloatingActionButton
import no.nordicsemi.android.nrfmesh.core.navigation.Screen

class BoundAppKeysScreen(
    override val title: String = "Bound Application Keys",
) : Screen {
    override val route = BoundAppKeysDestination.route
    override val showTopBar = true
    override val navigationIcon = Icons.AutoMirrored.Outlined.ArrowBack
    override val onNavigationIconClick: (() -> Unit) = {
        _buttons.tryEmit(Actions.BACK)
    }
    override val actions = emptyList<ActionMenuItem>()
    override val floatingActionButton = listOf(
        FloatingActionButton(
            icon = Icons.Outlined.Add,
            text = "Bind Key",
            contentDescription = "Bind Key",
            onClick = { _buttons.tryEmit(Actions.BIND_KEY) }
        )
    )
    override val showBottomBar = true

    private val _buttons = MutableSharedFlow<Actions>(extraBufferCapacity = 1)
    val buttons = _buttons.asSharedFlow()

    enum class Actions {
        BACK,
        BIND_KEY
    }
}