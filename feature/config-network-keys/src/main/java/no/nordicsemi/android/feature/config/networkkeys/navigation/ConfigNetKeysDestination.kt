package no.nordicsemi.android.feature.config.networkkeys.navigation

import android.os.Parcelable
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.feature.config.networkkeys.ConfigNetKeysScreen
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.Node

@Parcelize
data object ConfigNetKeysRoute : Parcelable

@Composable
fun ConfigNetKeysRoute(
    snackbarHostState: SnackbarHostState,
    isLocalProvisionerNode: Boolean,
    availableNetworkKeys: List<NetworkKey>,
    addedNetworkKeys: List<NetworkKey>,
    messageState: MessageState,
    onAddNetworkKeyClicked: () -> Unit,
    isKeyInUse:(NetworkKey) -> Boolean,
    navigateToNetworkKeys: () -> Unit,
    send: (AcknowledgedConfigMessage) -> Unit,
    resetMessageState: () -> Unit
) {
    ConfigNetKeysScreen(
        messageState = messageState,
        snackbarHostState = snackbarHostState,
        isLocalProvisionerNode = isLocalProvisionerNode,
        availableNetworkKeys = availableNetworkKeys,
        addedNetworkKeys = addedNetworkKeys,
        onAddNetworkKeyClicked = onAddNetworkKeyClicked,
        isKeyInUse = isKeyInUse,
        navigateToNetworkKeys = navigateToNetworkKeys,
        send = send,
        resetMessageState = resetMessageState
    )
}

