package no.nordicsemi.android.nrfmesh.feature.nodes.node

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.feature.config.networkkeys.navigation.ConfigNetKeysRoute
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.core.ui.PlaceHolder
import no.nordicsemi.android.nrfmesh.core.ui.isDetailPaneVisible
import no.nordicsemi.android.nrfmesh.core.ui.isExtraPaneVisible
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeysContentKey
import no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.navigation.ConfigAppKeysRoute
import no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.navigation.ConfigAppKeysScreenRoute
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeysContentKey
import no.nordicsemi.android.nrfmesh.feature.nodes.R
import no.nordicsemi.android.nrfmesh.feature.nodes.node.element.ElementScreen
import no.nordicsemi.android.nrfmesh.feature.nodes.node.navigation.ElementModelRoute
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.Node

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun NodeDetailsPane(
    snackbarHostState: SnackbarHostState,
    navigator: ThreePaneScaffoldNavigator<Any>,
    node: Node,
    nodeInfoListData: NodeInfoListData,
    availableNetworkKeys: List<NetworkKey>,
    addedNetworkKeys: List<NetworkKey>,
    isNetKeyInUse:(NetworkKey) -> Boolean,
    onAddNetworkKeyClicked: () -> Unit,
    navigateToNetworkKeys: () -> Unit,
    addedAppKeys: List<ApplicationKey>,
    addedApplicationKeys: List<ApplicationKey>,
    isAppKeyInUse: (ApplicationKey) -> Boolean,
    onAddAppKeyClicked: () -> Unit,
    navigateToApplicationKeys: () -> Unit,
    readApplicationKeys: () -> Unit,
    navigateToModel: (Model) -> Unit,
    send: (AcknowledgedConfigMessage) -> Unit,
    messageState: MessageState,
    resetMessageState: () -> Unit,
    save: () -> Unit,
) {
    when (val content = navigator.currentDestination?.contentKey) {
        is ElementModelRoute -> ElementScreen(
            element = node.element(address = content.address) ?: return,
            elementData = nodeInfoListData.elements.firstOrNull{ it.unicastAddress.address == content.address} ?: return,
            highlightSelectedItem = navigator.isDetailPaneVisible() &&
                    navigator.isExtraPaneVisible(),
            navigateToModel = navigateToModel,
            save = save
        )

        is ConfigNetKeysRoute, NetworkKeysContentKey -> ConfigNetKeysRoute(
            snackbarHostState = snackbarHostState,
            isLocalProvisionerNode = node.isLocalProvisioner,
            availableNetworkKeys = availableNetworkKeys,
            addedNetworkKeys = addedNetworkKeys,
            isKeyInUse = isNetKeyInUse,
            onAddNetworkKeyClicked = onAddNetworkKeyClicked,
            navigateToNetworkKeys = navigateToNetworkKeys,
            messageState = messageState,
            resetMessageState = resetMessageState,
            send = send
        )

        is ConfigAppKeysRoute, ApplicationKeysContentKey -> ConfigAppKeysScreenRoute(
            snackbarHostState = snackbarHostState,
            isLocalProvisionerNode = node.isLocalProvisioner,
            availableAppKeys = addedAppKeys,
            addedAppKeys = addedApplicationKeys,
            isKeyInUse = isAppKeyInUse,
            onAddAppKeyClicked = onAddAppKeyClicked,
            navigateToApplicationKeys = navigateToApplicationKeys,
            messageState = messageState,
            resetMessageState = resetMessageState,
            readApplicationKeys = readApplicationKeys,
            send = send
        )

        else -> PlaceHolder(
            modifier = Modifier.fillMaxSize(),
            imageVector = Icons.Outlined.Info,
            text = stringResource(R.string.label_select_node_item_rationale)
        )
    }
}