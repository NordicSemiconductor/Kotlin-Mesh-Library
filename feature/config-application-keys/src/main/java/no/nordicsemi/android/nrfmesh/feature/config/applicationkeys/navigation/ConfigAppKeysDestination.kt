package no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.navigation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.ConfigAppKeysRoute
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigAppKeyAdd
import no.nordicsemi.kotlin.mesh.core.model.Node

@Parcelize
data object ConfigAppKeysRoute : Parcelable

@Composable
fun ConfigAppKeysScreenRoute(
    node: Node,
    messageState: MessageState,
    onApplicationKeyClicked: () -> Unit,
    send: (AcknowledgedConfigMessage) -> Unit,
    resetMessageState: () -> Unit,
) {
    ConfigAppKeysRoute(
        node = node,
        messageState = messageState,
        onApplicationKeysClicked = onApplicationKeyClicked,
        onAddKeyClicked = { send(ConfigAppKeyAdd(key = it)) },
        send = send,
        resetMessageState = resetMessageState
    )
}

