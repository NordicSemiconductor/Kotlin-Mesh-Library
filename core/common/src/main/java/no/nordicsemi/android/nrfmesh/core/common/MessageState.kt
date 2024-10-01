package no.nordicsemi.android.nrfmesh.core.common

import no.nordicsemi.kotlin.mesh.core.messages.BaseMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigResponse
import no.nordicsemi.kotlin.mesh.core.messages.MeshMessage

/**
 * Defines the message state of a mesh message.
 */
sealed class MessageState {

    fun MessageState.isInProgress(): Boolean = this is Sending

    fun MessageState.didFail(): Boolean = this is Failed
}

/**
 * Defines a state where the message sending has not started yet.
 */
data object NotStarted : MessageState()

/**
 * Defines a state where the message sending has begun.
 *
 * @param message Message that is being sent.
 */
data class Sending(val message: BaseMeshMessage) : MessageState()

/**
 * Defines a state when a message sending has been completed successfully.
 *
 * @param message   Message that was sent.
 * @param response  Response received from the mesh node.
 */
data class Completed(
    val message: MeshMessage,
    val response: ConfigResponse? = null
) : MessageState()

/**
 * Define a state when hen a message sending has failed.
 *
 * @param message   Message that was sent.
 * @param error     Error that occurred while sending the message.
 */
data class Failed(
    val message: MeshMessage?,
    val error: Throwable
) : MessageState()
