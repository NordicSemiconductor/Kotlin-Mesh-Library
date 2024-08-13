package no.nordicsemi.android.nrfmesh.feature.settings.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import no.nordicsemi.android.nrfmesh.core.navigation.ActionItem
import no.nordicsemi.android.nrfmesh.core.navigation.Screen

class SettingsScreen(
    override val title: String = "Settings",
) : Screen {
    override val route: String
        get() = SettingsDestination.route
    override val showTopBar: Boolean
        get() = true
    override val navigationIcon: ImageVector?
        get() = null
    override val onNavigationIconClick: (() -> Unit)?
        get() = null
    override val actions: List<ActionItem>
        get() = emptyList()
    override val showBottomBar: Boolean
        get() = true
}