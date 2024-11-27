package no.nordicsemi.android.nrfmesh.feature.model

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddLink
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.nrfmesh.core.common.Failed
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.core.common.NotStarted.didFail
import no.nordicsemi.android.nrfmesh.core.common.NotStarted.isInProgress
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.MeshMessageStatusDialog
import no.nordicsemi.android.nrfmesh.feature.configurationserver.R
import no.nordicsemi.android.nrfmesh.feature.model.common.CommonInformation
import no.nordicsemi.android.nrfmesh.feature.model.common.ModelPublication
import no.nordicsemi.android.nrfmesh.feature.model.configurationServer.ConfigurationServerModel
import no.nordicsemi.android.nrfmesh.feature.model.navigation.ModelScreen
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.model.Model

@Composable
internal fun ModelRoute(
    appState: AppState,
    uiState: ModelScreenUiState,
    send: (AcknowledgedConfigMessage) -> Unit,
    navigateToBoundAppKeys: (Model) -> Unit,
    requestNodeIdentityStates: () -> Unit,
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
        navigateToBoundAppKeys = navigateToBoundAppKeys,
        requestNodeIdentityStates = requestNodeIdentityStates,
        resetMessageState = resetMessageState
    )
}

@Composable
internal fun ModelScreen(
    uiState: ModelScreenUiState,
    send: (AcknowledgedConfigMessage) -> Unit,
    navigateToBoundAppKeys: (Model) -> Unit,
    requestNodeIdentityStates: () -> Unit,
    resetMessageState: () -> Unit
) {
    when (uiState.modelState) {
        ModelState.Loading -> {}
        is ModelState.Success -> ModelInformation(
            messageState = uiState.messageState,
            nodeIdentityStates = uiState.nodeIdentityStates,
            model = uiState.modelState.model,
            send = send,
            navigateToBoundAppKeys = navigateToBoundAppKeys,
            requestNodeIdentityStates = requestNodeIdentityStates,
            resetMessageState = resetMessageState
        )

        is ModelState.Error -> {}
    }
}

@Composable
internal fun ModelInformation(
    messageState: MessageState,
    nodeIdentityStates: List<NodeIdentityStatus>,
    model: Model,
    send: (AcknowledgedConfigMessage) -> Unit,
    navigateToBoundAppKeys: (Model) -> Unit,
    requestNodeIdentityStates: () -> Unit,
    resetMessageState: () -> Unit
) {
    Column(modifier = Modifier.verticalScroll(state = rememberScrollState())) {
        AnimatedVisibility(visible = messageState.isInProgress()) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        CommonInformation(model = model)
        when {
            model.isConfigurationServer -> ConfigurationServerModel(
                messageState = messageState,
                model = model,
                nodeIdentityStates = nodeIdentityStates,
                send = send,
                requestNodeIdentityStates = requestNodeIdentityStates,
            )

            else -> {
                BoundApplicationKeys(model = model, navigateToBoundAppKeys = navigateToBoundAppKeys)
                ModelPublication(model = model, send = send)
            }
        }
    }

    when (messageState) {
        is Failed -> {
            MeshMessageStatusDialog(
                text = messageState.error.message ?: stringResource(R.string.label_unknown_error),
                showDismissButton = !messageState.didFail(),
                onDismissRequest = resetMessageState,
            )
        }

        else -> {

        }
    }
}

@Composable
internal fun BoundApplicationKeys(
    model: Model,
    navigateToBoundAppKeys: (Model) -> Unit
) {
    ElevatedCardItem(
        modifier = Modifier
            .padding(top = 8.dp)
            .padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.AddLink,
        title = stringResource(R.string.label_bind_application_keys),
        subtitle = "${model.boundApplicationKeys.size} key(s) are bound",
        onClick = {
            navigateToBoundAppKeys(model)
        }
    )
}
