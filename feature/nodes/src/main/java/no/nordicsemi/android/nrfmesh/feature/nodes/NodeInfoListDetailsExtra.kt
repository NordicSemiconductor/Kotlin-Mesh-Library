package no.nordicsemi.android.nrfmesh.feature.nodes

import androidx.compose.runtime.Composable
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.core.common.NodeIdentityStatus
import no.nordicsemi.android.nrfmesh.feature.model.navigation.ModelScreenRoute
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.ModelRoute
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.model.Node


@Composable
internal fun NodeInfoListDetailsExtra(
    content: Any?,
    node: Node,
    messageState: MessageState,
    nodeIdentityStatus: List<NodeIdentityStatus>,
    highlightSelectedItem: Boolean,
    send: (AcknowledgedConfigMessage) -> Unit
) {
    when (content) {
        is ModelRoute -> {
            ModelScreenRoute(
                model = node.element(address = content.address)
                    ?.model(modelId = content.modelId)
                    ?: return,
                messageState = messageState,
                nodeIdentityStates = nodeIdentityStatus,
                send = send
            )
        }

        else -> {
            NodeInfoPlaceHolder()
        }
    }
}