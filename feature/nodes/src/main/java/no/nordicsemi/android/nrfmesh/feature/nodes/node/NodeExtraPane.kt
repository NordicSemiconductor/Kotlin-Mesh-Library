package no.nordicsemi.android.nrfmesh.feature.nodes.node

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.core.common.NodeIdentityStatus
import no.nordicsemi.android.nrfmesh.core.ui.PlaceHolder
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeysContent
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeysScreenRoute
import no.nordicsemi.android.nrfmesh.feature.model.navigation.ModelScreenRoute
import no.nordicsemi.android.nrfmesh.feature.nodes.R
import no.nordicsemi.android.nrfmesh.feature.nodes.node.navigation.ModelRouteKeyKey
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigAppKeyAdd
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.Node
import java.util.UUID


@Composable
internal fun NodeExtraPane(
    content: Any?,
    node: Node,
    messageState: MessageState,
    nodeIdentityStatus: List<NodeIdentityStatus>,
    send: (AcknowledgedConfigMessage) -> Unit,
    requestNodeIdentityStates: (Model) -> Unit,
    resetMessageState: () -> Unit,
    navigateToGroups: () -> Unit,
    navigateToConfigApplicationKeys: (UUID) -> Unit,
) {
    when (content) {
        is ModelRouteKeyKey -> ModelScreenRoute(
            model = node.element(address = content.address)
                ?.model(modelId = content.modelId)
                ?: return,
            messageState = messageState,
            nodeIdentityStates = nodeIdentityStatus,
            send = send,
            requestNodeIdentityStates = requestNodeIdentityStates,
            resetMessageState = resetMessageState,
            navigateToGroups = navigateToGroups,
            navigateToConfigApplicationKeys = navigateToConfigApplicationKeys
        )

        is ApplicationKeysContent -> ApplicationKeysScreenRoute(
            highlightSelectedItem = false,
            onApplicationKeyClicked = {
                send(
                    ConfigAppKeyAdd(
                        key = node.network?.applicationKey(it)
                            ?: throw IllegalStateException(
                                "Unable to find application key with index $it"
                            )
                    )
                )
            },
            navigateToKey = {},
            navigateUp = {}
        )

        else -> PlaceHolder(
            modifier = Modifier.fillMaxSize(),
            imageVector = Icons.Outlined.Info,
            text = stringResource(R.string.label_select_node_item_rationale)
        )
    }
}