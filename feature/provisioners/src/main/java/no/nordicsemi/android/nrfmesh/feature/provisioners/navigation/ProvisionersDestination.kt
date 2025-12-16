@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.provisioners.navigation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.feature.provisioners.ProvisionersRoute
import no.nordicsemi.android.nrfmesh.feature.provisioners.ProvisionersViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
@Parcelize
data object ProvisionersContent : Parcelable

@OptIn(ExperimentalUuidApi::class)
@Composable
fun ProvisionersScreenRoute(
    highlightSelectedItem: Boolean,
    onProvisionerClicked: (Uuid) -> Unit,
    navigateToProvisioner: (Uuid) -> Unit,
    navigateUp: () -> Unit,
) {
    val viewModel = hiltViewModel<ProvisionersViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProvisionersRoute(
        highlightSelectedItem = highlightSelectedItem,
        selectedProvisionerUuid = uiState.selectedProvisionerUuid,
        provisioners = uiState.provisioners,
        onAddProvisionerClicked = viewModel::addProvisioner,
        onProvisionerClicked = {
            viewModel.selectProvisioner(uuid = it)
            onProvisionerClicked(it)
        },
        navigateToProvisioner = {
            viewModel.selectProvisioner(uuid = it)
            navigateToProvisioner(it)
        },
        onSwiped = {
            viewModel.onSwiped(provisioner = it)
            if (uiState.selectedProvisionerUuid == it.uuid) {
                navigateUp()
            }
        },
        onUndoClicked = viewModel::onUndoSwipe,
        remove = viewModel::remove
    )
}