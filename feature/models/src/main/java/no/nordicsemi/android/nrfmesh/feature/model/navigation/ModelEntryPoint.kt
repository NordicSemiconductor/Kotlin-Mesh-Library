package no.nordicsemi.android.nrfmesh.feature.model.navigation

import android.os.Parcelable
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.core.common.NodeIdentityStatus
import no.nordicsemi.android.nrfmesh.core.data.models.ModelData
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.GroupsKey
import no.nordicsemi.android.nrfmesh.core.navigation.Navigator
import no.nordicsemi.android.nrfmesh.feature.model.ModelScreen
import no.nordicsemi.android.nrfmesh.feature.model.ModelState
import no.nordicsemi.android.nrfmesh.feature.model.ModelViewModel
import no.nordicsemi.kotlin.data.HexString
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.MeshMessage
import no.nordicsemi.kotlin.mesh.core.model.Model
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Parcelize
data class ModelRouteKey(val modelId: UInt, val address: UShort) : Parcelable

@Serializable
data class ModelKey(val address: HexString, val modelId: HexString) : NavKey

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalUuidApi::class)
fun EntryProviderScope<NavKey>.modelEntry(appState: AppState, navigator: Navigator) {
    entry<ModelKey>(metadata = ListDetailSceneStrategy.extraPane()) { key ->
        val address = key.address
        val modelId = key.modelId
        val modelRouteKey = ModelRouteKey(
            modelId = modelId.toUInt(radix = 16),
            address = address.toUShort(radix = 16)
        )
        val viewModel = hiltViewModel<ModelViewModel, ModelViewModel.Factory>(
            key = modelRouteKey.toString()
        ) {
            it.create(modelRouteKey = modelRouteKey)
        }
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ModelScreen(
            snackbarHostState = appState.snackbarHostState,
            modelState = uiState.modelState,
            messageState = uiState.messageState,
            nodeIdentityStates = uiState.nodeIdentityStates,
            requestNodeIdentityStates = viewModel::requestNodeIdentityStates,
            onAddGroupClicked = {},
            resetMessageState = viewModel::resetMessageState,
            navigateToGroups = { navigator.navigate(key = GroupsKey) },
            navigateToConfigApplicationKeys = {},
            send = viewModel::send,
            sendApplicationMessage = viewModel::sendApplicationMessage
        )
    }
}