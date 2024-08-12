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
    onBackPressed: () -> Unit,
    onNavigateToDestination: (MeshNavigationDestination, String) -> Unit
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
        onNavigateToUnicastRanges = { destination, provisionerUuid ->
            onNavigateToDestination(destination, provisionerUuid)
        },
        onNavigateToGroupRanges = { destination, provisionerUuid ->
            onNavigateToDestination(destination, provisionerUuid)
        },
        onNavigateToSceneRanges = { destination, provisionerUuid ->
            onNavigateToDestination(destination, provisionerUuid)
        },
        onBackPressed = onBackPressed
    )
}