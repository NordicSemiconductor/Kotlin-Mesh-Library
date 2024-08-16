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
    override val route = GroupsDestination.route
    override val showTopBar = true
    override val navigationIcon = null
    override val onNavigationIconClick: (() -> Unit)? = null
    override val actions = emptyList<ActionMenuItem>()
    override val floatingActionButton = listOf(
        FloatingActionButton(
            icon = Icons.Outlined.Add,
            text = "Add Group",
            contentDescription = "Add Group",
            onClick = {

            }
        )
    )
    override val showBottomBar = true
}