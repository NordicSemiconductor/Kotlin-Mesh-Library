package no.nordicsemi.android.nrfmesh.core.navigation

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A screen represents a single screen in the application.
 *
 * @property route                   Route of the screen.
 * @property showTopBar              True if the app bar should be displayed.
 * @property navigationIcon          Navigation icon to be displayed on the screen.
 * @property onNavigationIconClick   Action to be performed when the navigation icon is clicked.
 * @property title                   Title of the screen.
 * @property actions                 Actions to be displayed on the screen.
 * @property showBottomBar           True if the bottom bar should be displayed.
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

/**
 * Defines a floating action button.
 *
 * @property icon                Icon of the floating action button.
 * @property text                Text to be displayed on the floating action button.
 * @property contentDescription  Content description of the floating action button.
 * @property onClick             Action to be performed when the floating action button is clicked.
 */
data class FloatingActionButton(
    val icon: ImageVector,
    val text: String,
    val contentDescription: String? = null,
    val onClick: () -> Unit
)

/**
 * An action menu item represents a single action that can be performed on the screen.
 *
 * @property title    Title of the action.
 * @property onClick  Action to be performed when the action is clicked.
 */
sealed interface ActionMenuItem {
    val title: String
    val onClick: () -> Unit

    /**
     * An icon menu item represents a single action that can be performed on the screen with an
     * icon.
     *
     * @property icon               Icon of the action.
     * @property contentDescription Content description of the action.
     */
    sealed interface IconMenuItem : ActionMenuItem {
        val icon: ImageVector
        val contentDescription: String?

        /**
         * An action that is always shown in the menu.
         *
         * @property title              Title of the action.
         * @property contentDescription Content description of the action.
         * @property onClick            Action to be performed when the action is clicked.
         * @property icon               Icon of the action.
         * @constructor Creates an always shown icon menu item.
         */
        data class AlwaysShown(
            override val title: String,
            override val contentDescription: String?,
            override val onClick: () -> Unit,
            override val icon: ImageVector,
        ) : IconMenuItem

        /**
         * An action that is shown only if the room is available.
         *
         * @property title              Title of the action.
         * @property contentDescription Content description of the action.
         * @property onClick            Action to be performed when the action is clicked.
         * @property icon               Icon of the action.
         * @constructor Creates a shown if room icon menu item.
         */
        data class ShownIfRoom(
            override val title: String,
            override val contentDescription: String?,
            override val onClick: () -> Unit,
            override val icon: ImageVector,
        ) : IconMenuItem

    }

    /**
     * An action that is shown only if the room is not available.
     *
     * @property title              Title of the action.
     * @property onClick            Action to be performed when the action is clicked.
     * @constructor Creates a shown if room icon menu item.
     */
    data class NeverShown(
        override val title: String,
        override val onClick: () -> Unit,
    ) : ActionMenuItem
}