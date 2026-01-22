package no.nordicsemi.android.nrfmesh.feature.groups.group.controls.navigation

import android.os.Parcelable
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.Navigator
import no.nordicsemi.android.nrfmesh.feature.groups.group.controls.GroupControlsScreen
import no.nordicsemi.android.nrfmesh.feature.groups.group.controls.GroupControlsViewModel
import no.nordicsemi.kotlin.data.HexString
import kotlin.uuid.ExperimentalUuidApi

@Parcelize
@Serializable
data class GroupControlsKey(val address: HexString, val modelId: HexString) : NavKey, Parcelable

@OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.groupControlsEntry(appState: AppState, navigator: Navigator) {
    entry<GroupControlsKey>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
        val address = key.address
        val id = key.modelId
        val viewModel = hiltViewModel<GroupControlsViewModel, GroupControlsViewModel.Factory>(
            key = "$address:$id"
        ) {
            it.create(key = "$address:$id")
        }
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        GroupControlsScreen(
            uiState = uiState,
            send = viewModel::send
        )
    }
}