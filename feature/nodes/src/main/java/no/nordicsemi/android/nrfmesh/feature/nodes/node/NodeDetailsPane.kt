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
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.feature.config.networkkeys.navigation.ConfigNetKeysRoute
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.core.ui.isDetailPaneVisible
import no.nordicsemi.android.nrfmesh.core.ui.isExtraPaneVisible
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeysContent
import no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.navigation.ConfigAppKeysRoute
import no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.navigation.ConfigAppKeysScreenRoute
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeysContent
import no.nordicsemi.android.nrfmesh.feature.nodes.R
import no.nordicsemi.android.nrfmesh.feature.nodes.node.element.ElementScreen
import no.nordicsemi.android.nrfmesh.feature.nodes.node.navigation.ElementModelRouteKey
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.Node

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun NodeDetailsPane(
    navigator: ThreePaneScaffoldNavigator<Any>,
    node: Node,
    availableNetworkKeys: List<NetworkKey>,
    onAddNetworkKeyClicked: () -> Unit,
    navigateToNetworkKeys: () -> Unit,
    availableApplicationKeys: List<ApplicationKey>,
    onAddAppKeyClicked: () -> Unit,
    navigateToApplicationKeys: () -> Unit,
    navigateToModel: (Model) -> Unit,
    send: (AcknowledgedConfigMessage) -> Unit,
    messageState: MessageState,
    resetMessageState: () -> Unit,
    save: () -> Unit,
) {
    when (val content = navigator.currentDestination?.contentKey) {
        is ElementModelRouteKey -> ElementScreen(
            element = node.element(address = content.address) ?: return,
            highlightSelectedItem = navigator.isDetailPaneVisible() &&
                    navigator.isExtraPaneVisible(),
            navigateToModel = navigateToModel,
            save = save
        )

        is ConfigNetKeysRoute, NetworkKeysContent -> ConfigNetKeysRoute(
            addedNetworkKeys = node.networkKeys,
            availableNetworkKeys = availableNetworkKeys,
            onAddNetworkKeyClicked = onAddNetworkKeyClicked,
            navigateToNetworkKeys = navigateToNetworkKeys,
            messageState = messageState,
            resetMessageState = resetMessageState,
            send = send
        )

        is ConfigAppKeysRoute, ApplicationKeysContent -> ConfigAppKeysScreenRoute(
            node = node,
            availableAppKeys = availableApplicationKeys,
            onAddAppKeyClicked = onAddAppKeyClicked,
            navigateToApplicationKeys = navigateToApplicationKeys,
            messageState = messageState,
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