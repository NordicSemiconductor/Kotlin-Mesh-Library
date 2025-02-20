package no.nordicsemi.android.nrfmesh.feature.groups.navigation

import android.os.Parcelable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.feature.groups.GroupListDetailScreen
import no.nordicsemi.android.nrfmesh.feature.groups.GroupViewModel
import no.nordicsemi.kotlin.data.HexString
import no.nordicsemi.kotlin.mesh.core.model.PrimaryGroupAddress

@Parcelize
@Serializable
data class GroupRoute(val address: HexString) : Parcelable

fun NavController.navigateToGroup(address: PrimaryGroupAddress, navOptions: NavOptions? = null) =
    navigate(
        route = GroupRoute(address.toHexString()),
        navOptions = navOptions
    )

fun NavGraphBuilder.groupGraph() {
    composable<GroupRoute> {
        val viewModel = hiltViewModel<GroupViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        GroupListDetailScreen(
            messageState = uiState.messageState,
            uiState = uiState.groupState,
            save = viewModel::save
        )
    }
}