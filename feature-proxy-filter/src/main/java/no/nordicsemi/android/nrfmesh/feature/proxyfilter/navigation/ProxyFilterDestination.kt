package no.nordicsemi.android.nrfmesh.feature.proxyfilter.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination

object ProxyFilterDestination : MeshNavigationDestination {
    override val route: String = "proxy_filter_route"
    override val destination: String = "proxy_filter_destination"
    const val authorIdArg = "authorId"
}

fun NavGraphBuilder.proxyFilterGraph() {
    composable(route = ProxyFilterDestination.route) {

    }
}