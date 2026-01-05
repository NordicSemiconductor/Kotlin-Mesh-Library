package no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.navigation

import android.os.Parcelable
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.ConfigAppKeysScreen
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.Node

@Parcelize
data object ConfigAppKeysRoute : Parcelable

@Composable
fun ConfigAppKeysScreenRoute(
    snackbarHostState: SnackbarHostState,
    node: Node,
    availableAppKeys: List<ApplicationKey>,
    onAddAppKeyClicked: () -> Unit,
    navigateToApplicationKeys: () -> Unit,
    readApplicationKeys: () -> Unit,
    messageState: MessageState,
    send: (AcknowledgedConfigMessage) -> Unit,
    resetMessageState: () -> Unit,
) {
    ConfigAppKeysScreen(
        snackbarHostState = snackbarHostState,
        node = node,
        availableApplicationKeys = availableAppKeys,
        onAddAppKeyClicked = onAddAppKeyClicked,
        navigateToApplicationKeys = navigateToApplicationKeys,
        readApplicationKeys = readApplicationKeys,
        messageState = messageState,
        send = send,
        resetMessageState = resetMessageState
    )
}

