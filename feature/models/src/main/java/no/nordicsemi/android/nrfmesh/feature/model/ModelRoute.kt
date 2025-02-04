package no.nordicsemi.android.nrfmesh.feature.model

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddLink
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.nrfmesh.core.common.Completed
import no.nordicsemi.android.nrfmesh.core.common.Failed
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.core.common.NodeIdentityStatus
import no.nordicsemi.android.nrfmesh.core.common.NotStarted.didFail
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.MeshMessageStatusDialog
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.feature.model.common.CommonInformation
import no.nordicsemi.android.nrfmesh.feature.model.common.ModelPublication
import no.nordicsemi.android.nrfmesh.feature.model.configurationServer.ConfigurationServerModel
import no.nordicsemi.android.nrfmesh.feature.models.R
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigStatusMessage
import no.nordicsemi.kotlin.mesh.core.model.Model

@Composable
internal fun ModelRoute(
    messageState: MessageState,
    nodeIdentityStates: List<NodeIdentityStatus>,
    model: Model,
    send: (AcknowledgedConfigMessage) -> Unit,
    navigateToBoundAppKeys: (Model) -> Unit,
    requestNodeIdentityStates: () -> Unit,
    resetMessageState: () -> Unit,
    onAddGroupClicked: () -> Unit,
) {
    Column(
        modifier = Modifier.verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        SectionTitle(
            title = stringResource(R.string.label_model)
        )
        CommonInformation(model = model)
        when {
            model.isConfigurationServer -> ConfigurationServerModel(
                messageState = messageState,
                model = model,
                nodeIdentityStates = nodeIdentityStates,
                send = send,
                requestNodeIdentityStates = requestNodeIdentityStates,
                onAddGroupClicked = onAddGroupClicked,
            )

            else -> {
                BoundApplicationKeys(model = model, navigateToBoundAppKeys = navigateToBoundAppKeys)
                ModelPublication(
                    messageState = messageState,
                    model = model,
                    send = send
                )
            }
        }
    }

    when (messageState) {
        is Failed -> MeshMessageStatusDialog(
            text = messageState.error.message ?: stringResource(R.string.label_unknown_error),
            showDismissButton = !messageState.didFail(),
            onDismissRequest = resetMessageState,
        )

        is Completed -> messageState.response?.let {
            when (it is ConfigStatusMessage) {
                true -> MeshMessageStatusDialog(
                    text = it.message,
                    showDismissButton = messageState.didFail(),
                    onDismissRequest = resetMessageState,
                )

                else -> { /* Do nothing */ }
            }
        }

        else -> {}
    }
}

@Composable
internal fun BoundApplicationKeys(
    model: Model,
    navigateToBoundAppKeys: (Model) -> Unit,
) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.AddLink,
        title = stringResource(R.string.label_bind_application_keys),
        subtitle = "${model.boundApplicationKeys.size} key(s) are bound",
        onClick = { navigateToBoundAppKeys(model) }
    )
}
