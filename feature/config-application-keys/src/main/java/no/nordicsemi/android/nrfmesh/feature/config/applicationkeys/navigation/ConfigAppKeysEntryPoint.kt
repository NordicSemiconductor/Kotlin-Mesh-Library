package no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.navigation

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
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.ClickableSetting
import no.nordicsemi.android.nrfmesh.core.navigation.Navigator
import no.nordicsemi.android.nrfmesh.core.navigation.SettingsKey
import no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.ConfigAppKeysScreen
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey

@Serializable
data object ConfigAppKeysRoute : NavKey

@Serializable
data class ConfigAppKeysKey(val uuid: String) : NavKey

@Composable
fun ConfigAppKeysScreenRoute(
    snackbarHostState: SnackbarHostState,
    isLocalProvisionerNode: Boolean,
    availableAppKeys: List<ApplicationKey>,
    addedAppKeys: List<ApplicationKey>,
    onAddAppKeyClicked: () -> Unit,
    navigateToApplicationKeys: () -> Unit,
    readApplicationKeys: () -> Unit,
    isKeyInUse: (ApplicationKey) -> Boolean,
    messageState: MessageState,
    send: (AcknowledgedConfigMessage) -> Unit,
    resetMessageState: () -> Unit,
) {
    ConfigAppKeysScreen(
        snackbarHostState = snackbarHostState,
        isLocalProvisionerNode = isLocalProvisionerNode,
        availableApplicationKeys = availableAppKeys,
        addedApplicationKeys = addedAppKeys,
        onAddAppKeyClicked = onAddAppKeyClicked,
        navigateToApplicationKeys = navigateToApplicationKeys,
        readApplicationKeys = readApplicationKeys,
        isKeyInUse = isKeyInUse,
        messageState = messageState,
        send = send,
        resetMessageState = resetMessageState
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.configAppKeysEntry(
    appState: AppState,
    navigator: Navigator,
) {
    entry<ConfigAppKeysKey>(
        metadata = ListDetailSceneStrategy.detailPane()
    ) {
        val uuid = it.uuid
        val viewModel = hiltViewModel<ConfigAppKeysViewModel, ConfigAppKeysViewModel.Factory>(key = uuid) {
                it.create(uuid = uuid)
            }
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ConfigAppKeysScreen(
            snackbarHostState = appState.snackbarHostState,
            isLocalProvisionerNode = uiState.isLocalProvisionerNode,
            availableApplicationKeys = uiState.availableAppKeys,
            addedApplicationKeys = uiState.addedAppKeys,
            onAddAppKeyClicked = viewModel::addApplicationKey,
            navigateToApplicationKeys = {
                navigator.navigate(key = SettingsKey(setting = ClickableSetting.APPLICATION_KEYS))
            },
            readApplicationKeys = viewModel::readApplicationKeys,
            isKeyInUse = viewModel::isKeyInUse,
            messageState = uiState.messageState,
            send = viewModel::send,
            resetMessageState = viewModel::resetMessageState
        )
    }
}