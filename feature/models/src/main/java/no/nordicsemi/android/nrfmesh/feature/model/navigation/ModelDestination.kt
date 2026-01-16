package no.nordicsemi.android.nrfmesh.feature.model.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.core.common.NodeIdentityStatus
import no.nordicsemi.android.nrfmesh.core.data.models.ModelData
import no.nordicsemi.android.nrfmesh.feature.model.ModelScreen
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.MeshMessage
import no.nordicsemi.kotlin.mesh.core.model.Model
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
fun ModelScreenRoute(
    snackbarHostState: SnackbarHostState,
    model: Model,
    modelData: ModelData,
    messageState: MessageState,
    nodeIdentityStates: List<NodeIdentityStatus>,
    requestNodeIdentityStates: (Model) -> Unit = {},
    resetMessageState: () -> Unit,
    navigateToGroups: () -> Unit,
    navigateToConfigApplicationKeys: (Uuid) -> Unit,
    send: (AcknowledgedConfigMessage) -> Unit,
    sendApplicationMessage: (Model, MeshMessage) -> Unit,
) {
    ModelScreen(
        snackbarHostState = snackbarHostState,
        model = model,
        modelData = modelData,
        messageState = messageState,
        nodeIdentityStates = nodeIdentityStates,
        requestNodeIdentityStates = requestNodeIdentityStates,
        onAddGroupClicked = {},
        resetMessageState = resetMessageState,
        navigateToGroups = navigateToGroups,
        navigateToConfigApplicationKeys = navigateToConfigApplicationKeys,
        send = send,
        sendApplicationMessage = sendApplicationMessage
    )
}