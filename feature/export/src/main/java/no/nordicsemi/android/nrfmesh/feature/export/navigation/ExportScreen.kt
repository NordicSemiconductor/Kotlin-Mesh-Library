package no.nordicsemi.android.nrfmesh.feature.export.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import no.nordicsemi.android.nrfmesh.core.navigation.ActionMenuItem
import no.nordicsemi.android.nrfmesh.core.navigation.FloatingActionButton
import no.nordicsemi.android.nrfmesh.core.navigation.Screen

class ExportScreen(override val title: String) : Screen {
    override val route = ExportDestination.route
    override val showTopBar = true
    override val navigationIcon = null
    override val onNavigationIconClick: (() -> Unit)? = null
    override val actions = emptyList<ActionMenuItem>()
    override val floatingActionButton = emptyList<FloatingActionButton>()
    override val showBottomBar = true
}