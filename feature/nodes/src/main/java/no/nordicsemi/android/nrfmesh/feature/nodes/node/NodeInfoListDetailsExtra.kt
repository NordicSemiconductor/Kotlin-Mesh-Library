package no.nordicsemi.android.nrfmesh.feature.nodes.node

import androidx.compose.runtime.Composable
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.core.common.NodeIdentityStatus
import no.nordicsemi.android.nrfmesh.feature.model.navigation.ModelScreenRoute
import no.nordicsemi.android.nrfmesh.feature.nodes.node.navigation.ModelRouteKeyKey
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.model.Node


@Composable
internal fun NodeInfoListDetailsExtra(
    content: Any?,
    node: Node,
    messageState: MessageState,
    nodeIdentityStatus: List<NodeIdentityStatus>,
    send: (AcknowledgedConfigMessage) -> Unit,
    resetMessageState: () -> Unit
) {
    when (content) {
        is ModelRouteKeyKey -> {
            ModelScreenRoute(
                model = node.element(address = content.address)
                    ?.model(modelId = content.modelId)
                    ?: return,
                messageState = messageState,
                nodeIdentityStates = nodeIdentityStatus,
                send = send,
                resetMessageState = resetMessageState
            )
        }

        else -> {
            NodeInfoPlaceHolder()
        }
    }
}