package no.nordicsemi.kotlin.mesh.core.layers.access

import no.nordicsemi.kotlin.mesh.core.messages.MeshMessage
import no.nordicsemi.kotlin.mesh.core.model.Element
import no.nordicsemi.kotlin.mesh.core.model.MeshAddress

/**
 * Defines a set of errors originating from the access layer.
 */
sealed class AccessError : Exception() {
    override fun toString() = when (this) {
        is InvalidSource -> "Local Provisioner does not have a Unicast Address specified."
        is InvalidElement -> "Element does not belong to the local node."
        is InvalidTtl -> "Invalid TTL."
        is InvalidDestination -> "Destination address unknown."
        is ModelNotBoundToAppKey -> "No Application Key bound to the given Model."
        is NoDeviceKey -> "Unknown Device Key."
        is NoNetworkKey -> "No Network Key."
        is CannotDelete -> "Cannot delete the last Network Key."
        is Busy -> "Unable to send a message to specified address. Another transfer in progress."
        is Timeout -> "Request timed out."
        is Cancelled -> "Message cancelled."
        is MessageSendingFailed -> "Message sending failed: ${error.message}"
        is CannotRelay -> "Network Key not known to the connected GATT Proxy."
        is InvalidKey -> "Cannot decrypt message with the given Key."
        is NoAppKeysBoundToModel -> "No Application Keys bound to the Model."
    }
}

/**
 * Thrown when a message is sent that is encrypted with a Network Key that is not known to the
 * connected GATT Proxy, or no GATT Proxy is connected.
 */
class CannotRelay : AccessError()

/**
 * Thrown when the target node cannot decrypt messages sent with the given Network Key.
 */
class InvalidKey : AccessError()

/**
 * Error thrown when the local Provisioner does not have a Unicast Address specified and is not able
 * to send requested message.
 */
class InvalidSource : AccessError()

/**
 * Thrown when trying to send a message using an Element that does not belong to the local
 * Provisioner's Node.
 */
class InvalidElement : AccessError()

/**
 * Thrown when the given TTL is not valid. Valid TTL must be 0 or in range 2...127.
 */
class InvalidTtl : AccessError()

/**
 * Thrown when the destination Address is not known and the library cannot determine the Network Key
 * to use.
 */
class InvalidDestination : AccessError()

/**
 * Thrown when trying to send a message from a Model that does not have any Application Key bound to
 * it.
 */
class ModelNotBoundToAppKey : AccessError()

/**
 * Thrown when trying to send a message from a Model that does not have any Application Key bound to
 * it. This is invoked when sending a message without specifying the Application Key. The library in
 * this situation would use the first application key bound to the model.
 */
class NoAppKeysBoundToModel : AccessError()

/**
 * Thrown if no Device Key was found, when trying to send a config message to a Node.
 */
class NoDeviceKey : AccessError()

/**
 * Thrown if no network key is found, when trying to send a mesh message.
 */
class NoNetworkKey : AccessError()

/**
 * Thrown when trying to send a message to an address to which another message is already being
 * sent.
 */
class Busy : AccessError()

/**
 * Error thrown when the Provisioner is trying to delete the last Network Key from the Node.
 */
class CannotDelete : AccessError()

/**
 * Thrown, when the acknowledgment has not been received until the time run out.
 */
class Timeout : AccessError()

/**
 * Thrown when sending the message was cancelled.
 */
class Cancelled : AccessError()

/**
 * Thrown when message sending failed.
 *
 * @property msg          Message that was being sent.
 * @property localElement Local element from which the message was being sent.
 * @property destination  Destination address.
 * @property error        Exception that caused the failure.
 */
data class MessageSendingFailed(
    val msg: MeshMessage,
    val localElement: Element,
    val destination: MeshAddress,
    val error: Exception,
) : AccessError()