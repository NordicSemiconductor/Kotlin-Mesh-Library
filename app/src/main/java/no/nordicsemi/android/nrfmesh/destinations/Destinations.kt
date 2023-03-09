package no.nordicsemi.android.nrfmesh.destinations

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material.icons.filled.Hive
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.GroupWork
import androidx.compose.material.icons.outlined.Hive
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import no.nordicsemi.android.common.navigation.DestinationId
import no.nordicsemi.android.common.navigation.createSimpleDestination
import no.nordicsemi.android.nrfmesh.R

val topLevelTabs = createSimpleDestination("top_level_tabs")
val nodesTab = createSimpleDestination("nodes_tab")
val groupsTab = createSimpleDestination("groups_tab")
val proxyFilterTab = createSimpleDestination("proxy_filter_tab")
val settingsTab = createSimpleDestination("settings_tab")


/**
 * Routes for the different top level destinations in the application. Navigation from one screen to
 * the next within a single destination will be handled directly in composables.
 *
 * @param destinationId        Destination id.
 * @param iconTextId           Text id for the icon.
 * @param selectedIcon         Selected icon.
 * @param unselectedIcon       Unselected icon.
 */
data class NavigationItem(
    val destinationId: DestinationId<Unit, *>,
    val iconTextId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

val navigationItems = listOf(
    NavigationItem(
        destinationId = nodesTab,
        iconTextId = R.string.label_nav_bar_nodes,
        selectedIcon = Icons.Filled.Hive,
        unselectedIcon = Icons.Outlined.Hive
    ),
    NavigationItem(
        destinationId = groupsTab,
        iconTextId = R.string.label_nav_bar_groups,
        selectedIcon = Icons.Filled.GroupWork,
        unselectedIcon = Icons.Outlined.GroupWork
    ),
    NavigationItem(
        destinationId = proxyFilterTab,
        iconTextId = R.string.label_nav_bar_proxy_filter,
        selectedIcon = Icons.Filled.Hub,
        unselectedIcon = Icons.Outlined.Hub
    ),
    NavigationItem(
        destinationId = settingsTab,
        iconTextId = R.string.label_nav_bar_settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
)
