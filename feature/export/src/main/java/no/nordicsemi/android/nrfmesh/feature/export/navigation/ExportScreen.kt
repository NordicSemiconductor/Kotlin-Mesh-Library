package no.nordicsemi.android.nrfmesh.feature.export.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import no.nordicsemi.android.nrfmesh.core.navigation.ActionMenuItem
import no.nordicsemi.android.nrfmesh.core.navigation.FloatingActionButton
import no.nordicsemi.android.nrfmesh.core.navigation.Screen

class ExportScreen(
    override val title: String = "Export",
) : Screen {
    override val route: String
        get() = ExportDestination.route
    override val showTopBar: Boolean
        get() = true
    override val navigationIcon: ImageVector?
        get() = null
    override val onNavigationIconClick: (() -> Unit)?
        get() = null
    override val actions: List<ActionMenuItem>
        get() = emptyList()
    override val floatingActionButton: FloatingActionButton?
        get() = null
    override val showBottomBar: Boolean
        get() = true
}