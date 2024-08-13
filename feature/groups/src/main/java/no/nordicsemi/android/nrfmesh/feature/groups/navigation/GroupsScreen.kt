package no.nordicsemi.android.nrfmesh.feature.groups.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.ui.graphics.vector.ImageVector
import no.nordicsemi.android.nrfmesh.core.navigation.ActionItem
import no.nordicsemi.android.nrfmesh.core.navigation.ActionMenuItem
import no.nordicsemi.android.nrfmesh.core.navigation.FloatingActionButton
import no.nordicsemi.android.nrfmesh.core.navigation.Screen

class GroupsScreen(
    override val title: String = "Groups",
) : Screen {
    override val route: String
        get() = GroupsDestination.route
    override val showTopBar: Boolean
        get() = true
    override val navigationIcon: ImageVector?
        get() = null
    override val onNavigationIconClick: (() -> Unit)?
        get() = null
    override val actions: List<ActionMenuItem>
        get() = emptyList()
    override val floatingActionButton: FloatingActionButton?
        get() = FloatingActionButton(
            icon = Icons.Outlined.Add,
            contentDescription = "Add node",
            onClick = {

            }
        )
    override val showBottomBar: Boolean
        get() = true
}