@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.layers

import no.nordicsemi.kotlin.mesh.core.messages.MeshMessage
import no.nordicsemi.kotlin.mesh.core.model.Address
import no.nordicsemi.kotlin.mesh.core.model.MeshAddress
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress

/**
 * MessageHandle is returned upon sending a mesh message that allows the message to be cancelled.
 *
 * Only segmented and acknowledged messages can be cancelled. Unsegmented unacknowledged messages
 * are sent instantaneously (depending on the connection interval and message size) and therefore
 * cannot be cancelled.
 *
 * The handle contains information about the message that was sent.
 *
 * @property message         Mesh message that was sent.
 * @property source          Source address of the message.
 * @property destination     Destination address of the message.
 * @constructor Creates a message handle.
 */
data class MessageHandle internal constructor(
    val message: MeshMessage,
    val source: UnicastAddress,
    val destination: MeshAddress,
    private val manager: NetworkManager
) {
    val opCode: UInt
        get() = message.opCode

    internal constructor(
        message: MeshMessage,
        source: Address,
        destination: MeshAddress,
        manager: NetworkManager
    ) : this(
        message = message,
        source = UnicastAddress(source),
        destination = destination,
        manager = manager
    )

    /**
     * Cancels sending the message.
     *
     * Only segmented and acknowledged messages can be cancelled. Unsegmented unacknowledged
     * messages are sent instantaneously (depending on the connection interval and message size) and
     * therefore cannot be cancelled.
     */
    suspend fun cancel() {
        manager.cancel(this)
    }
}
