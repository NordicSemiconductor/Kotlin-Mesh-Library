package no.nordicsemi.android.nrfmesh.core.navigation

import androidx.annotation.StringRes
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
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

data class MeshTopLevelNavItem(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @StringRes val iconTextId: Int,
    @StringRes val titleTextId: Int,
)

val NODES = MeshTopLevelNavItem(
    selectedIcon = Icons.Filled.Hive,
    unselectedIcon = Icons.Outlined.Hive,
    iconTextId = R.string.label_nav_bar_nodes,
    titleTextId = R.string.label_nav_bar_nodes
)

val GROUPS = MeshTopLevelNavItem(
    selectedIcon = Icons.Filled.GroupWork,
    unselectedIcon = Icons.Outlined.GroupWork,
    iconTextId = R.string.label_nav_bar_groups,
    titleTextId = R.string.label_nav_bar_groups,
)

val PROXY = MeshTopLevelNavItem(
    selectedIcon = Icons.Filled.Hub,
    unselectedIcon = Icons.Outlined.Hub,
    iconTextId = R.string.label_nav_bar_proxy,
    titleTextId = R.string.label_nav_bar_proxy,
)

val SETTINGS = MeshTopLevelNavItem(
    selectedIcon = Icons.Filled.Settings,
    unselectedIcon = Icons.Outlined.Settings,
    iconTextId = R.string.label_nav_bar_settings,
    titleTextId = R.string.label_nav_bar_settings,
)

@Serializable
object NodesKey: NavKey
@Serializable
object GroupsKey: NavKey
@Serializable
object ProxyKey: NavKey
@Serializable
data class SettingsKey(val setting: ClickableSetting? = null): NavKey

val MESH_TOP_LEVEL_NAV_ITEMS = mapOf (
    NodesKey to NODES,
    GroupsKey to GROUPS,
    ProxyKey to PROXY,
    SettingsKey(setting = null) to SETTINGS,
)