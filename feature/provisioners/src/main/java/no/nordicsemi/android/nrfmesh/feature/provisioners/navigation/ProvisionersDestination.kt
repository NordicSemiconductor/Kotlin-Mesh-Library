@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.provisioners.navigation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.provisioners.ProvisionersRoute
import no.nordicsemi.android.nrfmesh.feature.provisioners.ProvisionersViewModel
import no.nordicsemi.kotlin.mesh.core.model.Provisioner

@Parcelize
data object ProvisionersRoute : Parcelable

object ProvisionersDestination : MeshNavigationDestination {
    override val route: String = "provisioners_route"
    override val destination: String = "provisioners_destination"
}

fun NavGraphBuilder.provisionersGraph(
    appState: AppState,
    onNavigateToDestination: (MeshNavigationDestination, String) -> Unit,
    onBackPressed: () -> Unit
) {
    composable(route = ProvisionersDestination.route) {

    }
    provisionerGraph(
        appState = appState,
        onNavigateToUnicastRanges = onNavigateToDestination,
        onNavigateToGroupRanges = onNavigateToDestination,
        onNavigateToSceneRanges = onNavigateToDestination,
        onBackPressed = onBackPressed
    )
}

@Composable
fun ProvisionersScreenRoute(
    appState: AppState,
    provisioners: List<Provisioner>,
    navigateToProvisioner: (Provisioner) -> Unit,
    onBackPressed: () -> Unit
){
    val viewModel = hiltViewModel<ProvisionersViewModel>()
    ProvisionersRoute(
        appState = appState,
        provisioners = provisioners,
        navigateToProvisioner = navigateToProvisioner,
        onAddProvisionerClicked = viewModel::addProvisioner,
        onSwiped = viewModel::onSwiped,
        onUndoClicked = viewModel::onUndoSwipe,
        remove = viewModel::remove,
        onBackPressed = onBackPressed
    )
}