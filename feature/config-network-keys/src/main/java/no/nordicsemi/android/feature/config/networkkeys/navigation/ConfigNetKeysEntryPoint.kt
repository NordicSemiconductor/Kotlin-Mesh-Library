package no.nordicsemi.android.feature.config.networkkeys.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import no.nordicsemi.android.feature.config.networkkeys.ConfigNetKeysScreen
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.core.common.NotStarted
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.ClickableSetting
import no.nordicsemi.android.nrfmesh.core.navigation.Navigator
import no.nordicsemi.android.nrfmesh.core.navigation.SettingsKey
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey

@Serializable
data object ConfigNetKeysRoute : NavKey

@Serializable
data class ConfigNetKeysKey(val uuid: String) : NavKey

@Composable
fun ConfigNetKeysRoute(
    snackbarHostState: SnackbarHostState,
    isLocalProvisionerNode: Boolean,
    availableNetworkKeys: List<NetworkKey>,
    addedNetworkKeys: List<NetworkKey>,
    messageState: MessageState,
    onAddNetworkKeyClicked: () -> Unit,
    isKeyInUse: (NetworkKey) -> Boolean,
    navigateToNetworkKeys: () -> Unit,
    send: (AcknowledgedConfigMessage) -> Unit,
    resetMessageState: () -> Unit,
) {
    ConfigNetKeysScreen(
        messageState = messageState,
        snackbarHostState = snackbarHostState,
        isLocalProvisionerNode = isLocalProvisionerNode,
        availableNetworkKeys = availableNetworkKeys,
        addedNetworkKeys = addedNetworkKeys,
        onAddNetworkKeyClicked = onAddNetworkKeyClicked,
        isKeyInUse = isKeyInUse,
        navigateToNetworkKeys = navigateToNetworkKeys,
        send = send,
        resetMessageState = resetMessageState
    )
}


@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.configNetKeysEntry(
    appState: AppState,
    navigator: Navigator,
) {
    entry<ConfigNetKeysKey>(
        metadata = ListDetailSceneStrategy.detailPane()
    ) {
        val uuid = it.uuid
        val viewModel =
            hiltViewModel<ConfigNetKeysViewModel, ConfigNetKeysViewModel.Factory>(key = uuid) { it.create(uuid = uuid)
            }
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ConfigNetKeysScreen(
            messageState = uiState.messageState,
            snackbarHostState = appState.snackbarHostState,
            isLocalProvisionerNode = uiState.isLocalProvisionerNode,
            availableNetworkKeys = uiState.availableNetworkKeys,
            addedNetworkKeys = uiState.addedNetworkKeys,
            onAddNetworkKeyClicked = viewModel::addNetworkKey,
            isKeyInUse = viewModel::isKeyInUse,
            navigateToNetworkKeys = {
                navigator.navigate(key = SettingsKey(setting = ClickableSetting.NETWORK_KEYS))
            },
            send = viewModel::send,
            resetMessageState = viewModel::resetMessageState
        )
    }
}