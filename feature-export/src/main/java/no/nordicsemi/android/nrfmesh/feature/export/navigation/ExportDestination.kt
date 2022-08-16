package no.nordicsemi.android.nrfmesh.feature.export.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.export.ExportRoute

object ExportDestination : MeshNavigationDestination {
    override val route: String = "export_route"
    override val destination: String = "export_destination"
}

fun NavGraphBuilder.exportGraph(
    onBackPressed: () -> Unit
) {
    composable(route = ExportDestination.route) {
        ExportRoute(onBackPressed = onBackPressed)
    }
}