package no.nordicsemi.android.nrfmesh.feature.provisioners.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import no.nordicsemi.android.nrfmesh.core.navigation.ActionMenuItem
import no.nordicsemi.android.nrfmesh.core.navigation.FloatingActionButton
import no.nordicsemi.android.nrfmesh.core.navigation.Screen

class ProvisionersScreen(
    override val title: String = "Provisioners",
) : Screen {
    override val route = ProvisionersDestination.route
    override val showTopBar = true
    override val navigationIcon = Icons.AutoMirrored.Outlined.ArrowBack
    override val onNavigationIconClick: (() -> Unit) = {
        _buttons.tryEmit(Actions.BACK)
    }
    override val actions = emptyList<ActionMenuItem>()
    override val floatingActionButton = listOf(
        FloatingActionButton(
            icon = Icons.Outlined.Add,
            text = "Add Provisioner",
            contentDescription = "Add Provisioner",
            onClick = { _buttons.tryEmit(Actions.ADD_PROVISIONER) }
        )
    )

    private val _buttons = MutableSharedFlow<Actions>(extraBufferCapacity = 1)
    val buttons = _buttons.asSharedFlow()

    override val showBottomBar = true

    enum class Actions {
        ADD_PROVISIONER,
        BACK
    }
}