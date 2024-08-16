package no.nordicsemi.android.nrfmesh.feature.proxy.navigation

import no.nordicsemi.android.nrfmesh.core.navigation.ActionMenuItem
import no.nordicsemi.android.nrfmesh.core.navigation.FloatingActionButton
import no.nordicsemi.android.nrfmesh.core.navigation.Screen

class ProxyScreen(
    override val title: String = "Proxy"
) : Screen {
    override val route = ProxyDestination.route
    override val showTopBar = true
    override val navigationIcon = null
    override val onNavigationIconClick: (() -> Unit)? = null
    override val actions = emptyList<ActionMenuItem>()
    override val floatingActionButton = emptyList<FloatingActionButton>()
    override val showBottomBar = true
}