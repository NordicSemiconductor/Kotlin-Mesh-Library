package no.nordicsemi.android.nrfmesh.feature.configurationserver

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Groups3
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.nrfmesh.core.common.Completed
import no.nordicsemi.android.nrfmesh.core.common.Failed
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.core.common.NotStarted.didFail
import no.nordicsemi.android.nrfmesh.core.common.NotStarted.isInProgress
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.MeshAlertDialog
import no.nordicsemi.android.nrfmesh.core.ui.MeshMessageStatusDialog
import no.nordicsemi.android.nrfmesh.core.ui.SwitchWithIcon
import no.nordicsemi.android.nrfmesh.feature.configurationserver.navigation.ModelScreen
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.StatusMessage
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigFriendGet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigFriendSet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigGattProxyGet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigGattProxySet
import no.nordicsemi.kotlin.mesh.core.model.FeatureState
import no.nordicsemi.kotlin.mesh.core.model.Friend
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.ModelId
import no.nordicsemi.kotlin.mesh.core.model.Proxy
import no.nordicsemi.kotlin.mesh.core.model.SigModelId
import no.nordicsemi.kotlin.mesh.core.model.VendorModelId
import no.nordicsemi.kotlin.mesh.core.util.CompanyIdentifier

@Composable
internal fun ModelRoute(
    appState: AppState,
    uiState: ModelScreenUiState,
    send: (AcknowledgedConfigMessage) -> Unit,
    resetMessageState: () -> Unit,
    onBackPressed: () -> Unit
) {
    val screen = appState.currentScreen as? ModelScreen
    LaunchedEffect(key1 = screen) {
        screen?.buttons?.onEach {
            when (it) {
                ModelScreen.Actions.BACK -> onBackPressed()
            }
        }?.launchIn(this)
    }

    ModelScreen(
        uiState = uiState,
        send = send,
        resetMessageState = resetMessageState
    )
}

@Composable
internal fun ModelScreen(
    uiState: ModelScreenUiState,
    send: (AcknowledgedConfigMessage) -> Unit,
    resetMessageState: () -> Unit
) {
    when (uiState.modelState) {
        ModelState.Loading -> {}
        is ModelState.Success -> ModelInformation(
            messageState = uiState.messageState,
            model = uiState.modelState.model,
            proxy = uiState.modelState.model.parentElement?.parentNode?.features?.proxy,
            friend = uiState.modelState.model.parentElement?.parentNode?.features?.friend,
            send = send,
            resetMessageState = resetMessageState
        )

        is ModelState.Error -> {}
    }
}

@Composable
internal fun ModelInformation(
    messageState: MessageState,
    model: Model,
    proxy: Proxy?,
    friend: Friend?,
    send: (AcknowledgedConfigMessage) -> Unit,
    resetMessageState: () -> Unit
) {
    Column(modifier = Modifier.verticalScroll(state = rememberScrollState())) {
        AnimatedVisibility(visible = messageState.isInProgress()) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        CommonInformation(model = model)
        ConfigurationServer(
            proxy = proxy,
            friend = friend,
            send = send
        )
    }

    when (messageState) {
        is Failed -> {
            MeshMessageStatusDialog(
                text = messageState.error.message ?: stringResource(R.string.label_unknown_error),
                showDismissButton = !messageState.didFail(),
                onDismissRequest = resetMessageState,
            )
        }

        is Completed -> {
            messageState.response?.let {
                MeshMessageStatusDialog(
                    text = when (it) {
                        is StatusMessage -> it.message
                        else -> stringResource(id = R.string.label_success)
                    },
                    showDismissButton = messageState.didFail(),
                    onDismissRequest = resetMessageState,
                )
            }
        }

        else -> {

        }
    }
}

@Composable
private fun CommonInformation(model: Model) {
    NameRow(name = model.name)
    ModelIdRow(modelId = model.modelId.toHex(prefix0x = true))
    Company(modelId = model.modelId)
}

@Composable
private fun ConfigurationServer(
    proxy: Proxy?,
    friend: Friend?,
    send: (AcknowledgedConfigMessage) -> Unit
) {
    ProxyStateRow(proxy = proxy, send = send)
    FriendFeature(friend = friend, send = send)
}

@Composable
private fun NameRow(name: String) {
    ElevatedCardItem(
        modifier = Modifier
            .padding(top = 8.dp)
            .padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.Badge,
        title = stringResource(R.string.label_name),
        subtitle = name
    )
}

@Composable
private fun ModelIdRow(modelId: String) {
    ElevatedCardItem(
        modifier = Modifier
            .padding(top = 8.dp)
            .padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.Numbers,
        title = stringResource(R.string.label_model_identifier),
        subtitle = modelId
    )
}

@Composable
private fun Company(modelId: ModelId) {
    ElevatedCardItem(
        modifier = Modifier
            .padding(top = 8.dp)
            .padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.Work,
        title = stringResource(id = R.string.label_company),
        subtitle = when (modelId) {
            is SigModelId -> "Bluetooth SIG"
            is VendorModelId -> CompanyIdentifier.name(id = modelId.modelIdentifier)
                ?: stringResource(id = R.string.label_unknown)
        }
    )
}

@Composable
private fun ProxyStateRow(
    proxy: Proxy?,
    send: (AcknowledgedConfigMessage) -> Unit
) {
    var enabled by rememberSaveable {
        mutableStateOf(proxy?.state?.let { it == FeatureState.Enabled } ?: false)
    }
    var showProxyStateDialog by rememberSaveable { mutableStateOf(false) }
    ElevatedCardItem(
        modifier = Modifier
            .padding(top = 8.dp)
            .padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.Hub,
        title = stringResource(R.string.label_gatt_proxy),
        titleAction = {
            SwitchWithIcon(isChecked = enabled, onCheckedChange = {
                enabled = it
                if (!it) {
                    showProxyStateDialog = !showProxyStateDialog
                } else {
                    send(ConfigGattProxySet(state = FeatureState.Enabled))
                }
            })
        },
        subtitle = "Proxy state is ${if (enabled) "enabled" else "disabled"}",
        supportingText = stringResource(R.string.label_proxy_state_rationale)
    ) {
        OutlinedButton(onClick = { send(ConfigGattProxyGet()) }) {
            Text(text = stringResource(R.string.label_get_state))
        }
    }
    if (showProxyStateDialog) {
        MeshAlertDialog(onDismissRequest = {
            showProxyStateDialog = !showProxyStateDialog
            enabled = proxy?.state?.let { it == FeatureState.Enabled } ?: false
        },
            icon = Icons.Outlined.Hub,
            title = stringResource(R.string.label_disable_proxy_feature),
            text = stringResource(R.string.label_are_you_sure_rationale),
            iconColor = Color.Red,
            onConfirmClick = {
                enabled = false
                send(ConfigGattProxySet(state = FeatureState.Disabled))
                showProxyStateDialog = !showProxyStateDialog
            },
            onDismissClick = {
                showProxyStateDialog = !showProxyStateDialog
                enabled = proxy?.state?.let { it == FeatureState.Enabled } ?: false
            }
        )
    }
}

@Composable
private fun FriendFeature(
    friend: Friend?,
    send: (AcknowledgedConfigMessage) -> Unit
) {
    val enabled by rememberSaveable {
        mutableStateOf(friend?.state?.isEnabled ?: false)
    }
    ElevatedCardItem(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.Groups3,
        title = stringResource(R.string.label_friend_feature),
        titleAction = {
            SwitchWithIcon(isChecked = enabled, onCheckedChange = {
                send(ConfigFriendSet(enable = it))
            })
        },
        subtitle = "Friend feature is ${if (enabled) "enabled" else "disabled"}",
        supportingText = stringResource(R.string.label_proxy_state_rationale)
    ) {
        OutlinedButton(onClick = { send(ConfigFriendGet()) }) {
            Text(text = stringResource(R.string.label_get_state))
        }
    }
}