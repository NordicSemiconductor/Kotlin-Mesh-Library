package no.nordicsemi.android.nrfmesh.feature.model

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddLink
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.nrfmesh.core.common.Completed
import no.nordicsemi.android.nrfmesh.core.common.Failed
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.core.common.NodeIdentityStatus
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.MeshMessageStatusDialog
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.feature.bind.appkeys.BindAppKeysRoute
import no.nordicsemi.android.nrfmesh.feature.model.common.CommonInformation
import no.nordicsemi.android.nrfmesh.feature.model.common.Publication
import no.nordicsemi.android.nrfmesh.feature.model.common.Subscriptions
import no.nordicsemi.android.nrfmesh.feature.model.configurationServer.ConfigurationServer
import no.nordicsemi.android.nrfmesh.feature.models.R
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigStatusMessage
import no.nordicsemi.kotlin.mesh.core.model.Model
import java.util.UUID

@Composable
internal fun ModelRoute(
    messageState: MessageState,
    nodeIdentityStates: List<NodeIdentityStatus>,
    model: Model,
    send: (AcknowledgedConfigMessage) -> Unit,
    requestNodeIdentityStates: (Model) -> Unit,
    resetMessageState: () -> Unit,
    onAddGroupClicked: () -> Unit,
    navigateToGroups: () -> Unit,
    navigateToConfigApplicationKeys: (UUID) -> Unit,
) {
    Column(
        modifier = Modifier.verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        SectionTitle(
            modifier = Modifier.padding(top = 8.dp),
            title = stringResource(R.string.label_model)
        )
        CommonInformation(model = model)
        if (model.isConfigurationServer) {
            ConfigurationServer(
                messageState = messageState,
                model = model,
                nodeIdentityStates = nodeIdentityStates,
                send = send,
                requestNodeIdentityStates = requestNodeIdentityStates,
                onAddGroupClicked = onAddGroupClicked,
            )
        }
        if (model.supportsModelPublication == true || model.supportsModelSubscription == true) {
            BoundApplicationKeys(
                model = model,
                navigateToConfigApplicationKeys = navigateToConfigApplicationKeys,
                send = send
            )
        }
        if (model.supportsModelPublication == true) {
            Publication(messageState = messageState, model = model, send = send)
        }
        if (model.supportsModelSubscription == true) {
            Subscriptions(
                messageState = messageState,
                model = model,
                navigateToGroups = navigateToGroups,
                send = send
            )
        }
    }

    when (messageState) {
        is Failed -> MeshMessageStatusDialog(
            text = messageState.error.toString(),
            showDismissButton = !messageState.didFail(),
            onDismissRequest = resetMessageState,
        )

        is Completed -> {
            messageState.response?.takeIf {
                (it is ConfigStatusMessage && !it.isSuccess)
            }?.let {
                MeshMessageStatusDialog(
                    text = (messageState.response as ConfigStatusMessage).message,
                    showDismissButton = true,
                    onDismissRequest = resetMessageState,
                )
            }
        }

        else -> {

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BoundApplicationKeys(
    model: Model,
    navigateToConfigApplicationKeys: (UUID) -> Unit,
    send: (AcknowledgedConfigMessage) -> Unit,
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.AddLink,
        title = stringResource(R.string.label_bind_application_keys),
        subtitle = "${model.boundApplicationKeys.size} key(s) are bound",
        onClick = { showBottomSheet = !showBottomSheet }
    )
    if (showBottomSheet) {
        ModalBottomSheet(
            containerColor = MaterialTheme.colorScheme.surface,
            sheetState = bottomSheetState,
            onDismissRequest = { showBottomSheet = !showBottomSheet },
            content = {
                BindAppKeysRoute(
                    model = model,
                    send = send,
                    navigateToConfigApplicationKeys = navigateToConfigApplicationKeys
                )
            }
        )
    }
}
