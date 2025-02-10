package no.nordicsemi.android.nrfmesh.feature.nodes.node

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.feature.config.networkkeys.navigation.ConfigNetKeysRoute
import no.nordicsemi.android.feature.config.networkkeys.navigation.ConfigNetKeysScreenRoute
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeysRoute
import no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.navigation.ConfigAppKeysRoute
import no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.navigation.ConfigAppKeysScreenRoute
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeysRoute
import no.nordicsemi.android.nrfmesh.feature.nodes.R
import no.nordicsemi.android.nrfmesh.feature.nodes.node.element.navigation.ElementScreenRoute
import no.nordicsemi.android.nrfmesh.feature.nodes.node.navigation.ElementModelRouteKey
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.Node

@Composable
internal fun NodeInfoListDetails(
    content: Any?,
    node: Node,
    messageState: MessageState,
    highlightSelectedItem: Boolean,
    navigateToNetworkKeys: () -> Unit,
    navigateToApplicationKeys: () -> Unit,
    navigateToModel: (Model) -> Unit,
    send: (AcknowledgedConfigMessage) -> Unit,
    resetMessageState: () -> Unit,
    save: () -> Unit,
) {
    when (content) {
        is ElementModelRouteKey -> ElementScreenRoute(
            element = node.element(address = content.address) ?: return,
            highlightSelectedItem = highlightSelectedItem,
            navigateToModel = navigateToModel,
            save = save
        )

        is ConfigNetKeysRoute, NetworkKeysRoute -> ConfigNetKeysScreenRoute(
            node = node,
            messageState = messageState,
            navigateToNetworkKeys = navigateToNetworkKeys,
            resetMessageState = resetMessageState,
            send = send
        )

        is ConfigAppKeysRoute, ApplicationKeysRoute -> ConfigAppKeysScreenRoute(
            node = node,
            messageState = messageState,
            onApplicationKeyClicked = navigateToApplicationKeys,
            resetMessageState = resetMessageState,
            send = send
        )

        else -> NodeInfoPlaceHolder()
    }
}

@Composable
internal fun NodeInfoPlaceHolder(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.padding(top = 48.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 24.dp,
            bottomEnd = 0.dp,
            bottomStart = 0.dp
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                20.dp,
                alignment = Alignment.CenterVertically,
            ),
        ) {
            Icon(
                modifier = Modifier.size(96.dp),
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.label_select_node_item_rationale),
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}