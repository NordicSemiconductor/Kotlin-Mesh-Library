@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.provisioners.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.provisioners.ProvisionersRoute
import java.util.*

object ProvisionersDestination : MeshNavigationDestination {
    override val route: String = "provisioners_route"
    override val destination: String = "provisioners_destination"
}

fun NavGraphBuilder.provisionersGraph(
    onBackPressed: () -> Unit,
    onNavigateToProvisioner: (UUID) -> Unit
) {
    composable(route = ProvisionersDestination.route) {
        ProvisionersRoute(
            navigateToProvisioner = onNavigateToProvisioner,
            onBackClicked = onBackPressed
        )
    }
    provisionerGraph(onBackPressed = onBackPressed)
}