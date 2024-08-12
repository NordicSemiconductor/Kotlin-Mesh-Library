@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.provisioners.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.provisioners.ProvisionersRoute

object ProvisionersDestination : MeshNavigationDestination {
    override val route: String = "provisioners_route"
    override val destination: String = "provisioners_destination"
}

fun NavGraphBuilder.provisionersGraph(
    onNavigateToDestination: (MeshNavigationDestination, String) -> Unit,
    onBackPressed: () -> Unit
) {
    composable(route = ProvisionersDestination.route) {
        ProvisionersRoute(
            navigateToProvisioner = { provisionerUuid ->
                onNavigateToDestination(
                    ProvisionerDestination,
                    ProvisionerDestination.createNavigationRoute(provisionerUuid)
                )
            }
        )
    }
    provisionerGraph(
        onNavigateToUnicastRanges = onNavigateToDestination,
        onNavigateToGroupRanges = onNavigateToDestination,
        onNavigateToSceneRanges = onNavigateToDestination,
        onBackPressed = onBackPressed
    )
}