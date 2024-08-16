package no.nordicsemi.android.nrfmesh.core.navigation

import androidx.compose.ui.graphics.vector.ImageVector


/**
 * A screen represents a single screen in the application.
 *
 * @property route                   Route of the screen
 * @property showTopBar              True if the app bar should be displayed
 * @property navigationIcon          Navigation icon to be displayed on the screen
 * @property onNavigationIconClick   Action to be performed when the navigation icon is clicked
 * @property title                   Title of the screen
 * @property actions                 Actions to be displayed on the screen
 * @property showBottomBar           True if the bottom bar should be displayed
 */
interface Screen {
    val route: String
    val showTopBar: Boolean
    val navigationIcon: ImageVector?
    val onNavigationIconClick: (() -> Unit)?
    val title: String
    val actions: List<ActionMenuItem>
    val floatingActionButton: List<FloatingActionButton>
    val showBottomBar: Boolean
}

data class FloatingActionButton(
    val icon: ImageVector,
    val text: String,
    val contentDescription: String? = null,
    val onClick: () -> Unit
)

data class ActionItem(
    val icon: ImageVector,
    val contentDescription: String? = null,
    val enabled: Boolean = true,
    val onClick: () -> Unit
)

sealed interface ActionMenuItem {
    val title: String
    val onClick: () -> Unit

    // 1
    sealed interface IconMenuItem : ActionMenuItem {
        val icon: ImageVector
        val contentDescription: String?

        // 2
        data class AlwaysShown(
            override val title: String,
            override val contentDescription: String?,
            override val onClick: () -> Unit,
            override val icon: ImageVector,
        ) : IconMenuItem

        data class ShownIfRoom(
            override val title: String,
            override val contentDescription: String?,
            override val onClick: () -> Unit,
            override val icon: ImageVector,
        ) : IconMenuItem
    }

    data class NeverShown(
        override val title: String,
        override val onClick: () -> Unit,
    ) : ActionMenuItem
}