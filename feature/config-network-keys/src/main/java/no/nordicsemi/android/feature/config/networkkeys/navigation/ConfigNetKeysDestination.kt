package no.nordicsemi.android.feature.config.networkkeys.navigation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.feature.config.networkkeys.ConfigNetKeysRoute
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey

@Parcelize
data object ConfigNetKeysRoute : Parcelable

@Composable
fun ConfigNetKeysScreenRoute(
    networkKeys: List<NetworkKey>,
    messageState: MessageState,
    navigateToNetworkKeys: () -> Unit,
    send: (AcknowledgedConfigMessage) -> Unit,
    resetMessageState: () -> Unit
) {
    ConfigNetKeysRoute(
        networkKeys = networkKeys,
        messageState = messageState,
        navigateToNetworkKeys = navigateToNetworkKeys,
        onNetworkKeyClicked = { },
        send = send,
        resetMessageState = resetMessageState
    )
}

