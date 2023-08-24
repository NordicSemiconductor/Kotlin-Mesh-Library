@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.layers

import no.nordicsemi.kotlin.mesh.bearer.BearerError
import no.nordicsemi.kotlin.mesh.bearer.Transmitter
import no.nordicsemi.kotlin.mesh.core.messages.MeshMessage
import no.nordicsemi.kotlin.mesh.core.model.Address
import no.nordicsemi.kotlin.mesh.core.model.Element
import no.nordicsemi.kotlin.mesh.core.model.MeshAddress
import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager

/**
 * Defines events that are handled by the [NetworkManagerEvent].
 */
internal sealed class NetworkManagerEvent {

    /**
     * An event used to notify whenever a Mesh Message has been received from the mesh network.
     *
     * @property message      Received message.
     * @property source       Address of the Element from which the message was sent. This may be an
     *                        address
     * @property destination  Address to which the message was sent.
     * @constructor Creates a MessageReceived event.
     */
    data class MessageReceived(
        val message: MeshMessage,
        val source: Address,
        val destination: MeshAddress
    ) : NetworkManagerEvent()

    /**
     * An event used to notify whenever an unsegmented message was sent to the [Transmitter], or
     * when all segments of a segmented message targeting a Unicast Address were acknowledged by the
     * target Node.
     *
     * @param message        Received message.
     * @param localElement   Local Element from which the message was sent.
     * @param destination    Address to which the message was sent.
     * @constructor Creates a MessageSent event.
     */
    data class MessageSent(
        val message: MeshMessage,
        val localElement: Element,
        val destination: MeshAddress
    ) : NetworkManagerEvent()

    /**
     * Am event used to notify when a message failed to be sent to the target Node, or the response
     * for an acknowledged message hasn't been received before the time run out.
     *
     * For unsegmented unacknowledged messages this event will be emitted when the
     * [MeshNetworkManager.transmitter] is set to `null`, or has thrown an exception from
     * [MeshNetworkManager.transmitter] was set to `nil`, or has thrown an exception from
     * [Transmitter.send].
     *
     * For segmented unacknowledged messages targeting a Unicast Address, besides that, it may also
     * be called when sending timed out before all of the segments were acknowledged by the target
     * Node, or when the target Node is busy and not able to proceed the message at the moment.
     *
     * For acknowledged messages the callback will be called when the response has not been received
     * before the time set by [NetworkParameters.acknowledgementMessageTimeout] run out.
     * The message might have been retransmitted multiple times and might have been received by the
     * target Node. For acknowledged messages sent to a Group or Virtual Address this will be called
     * when the response has not been received from any Node.
     *
     * @param message            Message that has failed to be delivered.
     * @param localElement       Local Element used as a source of this message.
     * @param destination        Address to which the message was sent.
     * @param error              Error that occurred.
     *
     * @throws BearerError.Closed if the [MeshNetworkManager.transmitter] was not set.
     * @throws LowerTransportError.Busy if the target Node is busy and can't accept the message.
     * @throws LowerTransportError.Timeout if the segmented message targeting a Unicast Address was
     *                                     was not acknowledged before the
     *                                     [NetworkParameters.retransmissionLimit] was reached
     *                                     (for unacknowledged messages only).
     */
    data class MessageSendingFailed(
        val message: MeshMessage,
        val localElement: Element,
        val destination: MeshAddress,
        val error: BearerError
    ) : NetworkManagerEvent()

    /**
     * An event used to notify when the Network Configuration has changed.
     */
    data object NetworkDidChange : NetworkManagerEvent()

    /**
     * An event used to notify when the [ConfigurationNodeReset] message was received for the local
     * Node.
     *
     * The Node should forget the mesh network, all the keys, nodes, groups and scenes.
     *
     * A network might be created.
     */
    data object NetworkDidReset : NetworkManagerEvent()

}