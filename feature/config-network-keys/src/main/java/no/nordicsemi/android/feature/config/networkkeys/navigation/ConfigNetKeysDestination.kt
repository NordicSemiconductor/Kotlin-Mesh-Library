package no.nordicsemi.android.feature.config.networkkeys.navigation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.feature.config.networkkeys.ConfigNetKeysScreen
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey

@Parcelize
data object ConfigNetKeysRoute : Parcelable

@Composable
fun ConfigNetKeysRoute(
    addedNetworkKeys: List<NetworkKey>,
    availableNetworkKeys: List<NetworkKey>,
    messageState: MessageState,
    onAddNetworkKeyClicked: () -> Unit,
    navigateToNetworkKeys: () -> Unit,
    send: (AcknowledgedConfigMessage) -> Unit,
    resetMessageState: () -> Unit
) {
    ConfigNetKeysScreen(
        messageState = messageState,
        addedNetworkKeys = addedNetworkKeys,
        availableNetworkKeys = availableNetworkKeys,
        onAddNetworkKeyClicked = onAddNetworkKeyClicked,
        navigateToNetworkKeys = navigateToNetworkKeys,
        send = send,
        resetMessageState = resetMessageState
    )
}

