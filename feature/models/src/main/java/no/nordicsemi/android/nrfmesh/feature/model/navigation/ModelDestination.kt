package no.nordicsemi.android.nrfmesh.feature.model.navigation

import androidx.compose.runtime.Composable
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.core.common.NodeIdentityStatus
import no.nordicsemi.android.nrfmesh.feature.model.ModelRoute
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.model.Model
import java.util.UUID

@Composable
fun ModelScreenRoute(
    model: Model,
    messageState: MessageState,
    nodeIdentityStates: List<NodeIdentityStatus>,
    requestNodeIdentityStates: (Model) -> Unit = {},
    resetMessageState: () -> Unit,
    navigateToGroups: () -> Unit,
    navigateToConfigApplicationKeys: (UUID) -> Unit,
    send: (AcknowledgedConfigMessage) -> Unit
) {
    ModelRoute(
        model = model,
        messageState = messageState,
        nodeIdentityStates = nodeIdentityStates,
        requestNodeIdentityStates = requestNodeIdentityStates,
        onAddGroupClicked = {},
        resetMessageState = resetMessageState,
        navigateToGroups = navigateToGroups,
        navigateToConfigApplicationKeys = navigateToConfigApplicationKeys,
        send = send
    )
}