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
    navigateToProvisioner: (Uuid) -> Unit,
    navigateUp: () -> Unit,
) {
    val viewModel = hiltViewModel<ProvisionersViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProvisionersRoute(
        highlightSelectedItem = highlightSelectedItem,
        provisioners = uiState.provisioners,
        onAddProvisionerClicked = viewModel::addProvisioner,
        onSwiped = {
            viewModel.onSwiped(it)
            if (viewModel.isCurrentlySelectedProvisioner(uuid = it.uuid)) {
                navigateUp()
            }
        },
        onUndoClicked = viewModel::onUndoSwipe,
        remove = viewModel::remove,
        navigateToProvisioner = {
            viewModel.selectProvisioner(uuid = it)
            navigateToProvisioner(it)
        }
    )
}