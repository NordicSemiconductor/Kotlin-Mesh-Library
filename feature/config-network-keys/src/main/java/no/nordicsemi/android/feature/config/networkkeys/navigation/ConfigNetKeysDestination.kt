package no.nordicsemi.android.feature.config.networkkeys.navigation

import android.os.Parcelable
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.feature.config.networkkeys.ConfigNetKeysScreen
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.Node

@Parcelize
data object ConfigNetKeysRoute : Parcelable

@Composable
fun ConfigNetKeysRoute(
    snackbarHostState: SnackbarHostState,
    node: Node,
    availableNetworkKeys: List<NetworkKey>,
    messageState: MessageState,
    onAddNetworkKeyClicked: () -> Unit,
    navigateToNetworkKeys: () -> Unit,
    send: (AcknowledgedConfigMessage) -> Unit,
    resetMessageState: () -> Unit
) {
    ConfigNetKeysScreen(
        messageState = messageState,
        snackbarHostState = snackbarHostState,
        node = node,
        availableNetworkKeys = availableNetworkKeys,
        onAddNetworkKeyClicked = onAddNetworkKeyClicked,
        navigateToNetworkKeys = navigateToNetworkKeys,
        send = send,
        resetMessageState = resetMessageState
    )
}

