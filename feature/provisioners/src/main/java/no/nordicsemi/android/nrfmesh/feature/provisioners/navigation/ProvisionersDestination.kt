@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.provisioners.navigation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.provisioners.ProvisionersRoute
import no.nordicsemi.android.nrfmesh.feature.provisioners.ProvisionersViewModel
import java.util.UUID

@Parcelize
data object ProvisionersRoute : Parcelable

object ProvisionersDestination : MeshNavigationDestination {
    override val route: String = "provisioners_route"
    override val destination: String = "provisioners_destination"
}

@Composable
fun ProvisionersScreenRoute(
    highlightSelectedItem: Boolean,
    navigateToProvisioner: (UUID) -> Unit,
    navigateUp: () -> Unit,
){
    val viewModel = hiltViewModel<ProvisionersViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProvisionersRoute(
        highlightSelectedItem = highlightSelectedItem,
        provisioners = uiState.provisioners,
        onAddProvisionerClicked = viewModel::addProvisioner,
        onSwiped = {
            viewModel.onSwiped(it)
            if(viewModel.isCurrentlySelectedProvisioner(it.uuid)) {
                navigateUp()
            }
        },
        onUndoClicked = viewModel::onUndoSwipe,
        remove = viewModel::remove,
        navigateToProvisioner = {
            viewModel.selectProvisioner(it)
            navigateToProvisioner(it)
        }
    )
}