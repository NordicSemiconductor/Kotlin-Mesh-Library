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
    availableAppKeys: List<ApplicationKey>,
    addedAppKeys: List<ApplicationKey>,
    onAddAppKeyClicked: () -> Unit,
    navigateToApplicationKeys: () -> Unit,
    readApplicationKeys: () -> Unit,
    isKeyInUse:(ApplicationKey) -> Boolean,
    messageState: MessageState,
    send: (AcknowledgedConfigMessage) -> Unit,
    resetMessageState: () -> Unit,
) {
    ConfigAppKeysScreen(
        snackbarHostState = snackbarHostState,
        availableApplicationKeys = availableAppKeys,
        addedApplicationKeys = addedAppKeys,
        onAddAppKeyClicked = onAddAppKeyClicked,
        navigateToApplicationKeys = navigateToApplicationKeys,
        readApplicationKeys = readApplicationKeys,
        isKeyInUse = isKeyInUse,
        messageState = messageState,
        send = send,
        resetMessageState = resetMessageState
    )
}

