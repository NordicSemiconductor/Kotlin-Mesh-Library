package no.nordicsemi.kotlin.mesh.core.layers.access

/**
 * Defines a set of errors originating from the access layer.
 */
sealed class AccessError : Exception() {
    override fun toString() = when (this) {
        InvalidSource -> "Local Provisioner does not have a Unicast Address specified."
        InvalidElement -> "Element does not belong to the local node."
        InvalidTtl -> "Invalid TTL."
        InvalidDestination -> "Destination address unknown."
        ModelNotBoundToAppKey -> "No Application Key bound to the given Model."
        NoDeviceKey -> "Unknown Device Key."
        NoNetworkKey -> "No Network Key."
        CannotDelete -> "Cannot delete the last Network Key."
        Busy -> "Unable to send a message to specified address. Another transfer in progress."
        Timeout -> "Request timed out."
        Cancelled -> "Message cancelled."
    }
}

/**
 * Error thrown when the local Provisioner does not have a Unicast Address specified and is not able
 * to send requested message.
 */
data object InvalidSource : AccessError()

/**
 * Thrown when trying to send a message using an Element that does not belong to the local
 * Provisioner's Node.
 */
data object InvalidElement : AccessError()

/**
 * Thrown when the given TTL is not valid. Valid TTL must be 0 or in range 2...127.
 */
data object InvalidTtl : AccessError()

/**
 * Thrown when the destination Address is not known and the library cannot determine the Network Key
 * to use.
 */
data object InvalidDestination : AccessError()

/**
 * Thrown when trying to send a message from a Model that does not have any Application Key bound to
 * it.
 */
data object ModelNotBoundToAppKey : AccessError()

/**
 * Thrown if no Device Key was found, when trying to send a config message to a Node.
 */
data object NoDeviceKey : AccessError()

/**
 * Thrown if no network key is found, when trying to send a mesh message.
 */
data object NoNetworkKey : AccessError()

/**
 * Thrown when trying to send a message to an address to which another message is already being
 * sent.
 */
data object Busy : AccessError()

/**
 * Error thrown when the Provisioner is trying to delete the last Network Key from the Node.
 */
data object CannotDelete : AccessError()

/**
 * Thrown, when the acknowledgment has not been received until the time run out.
 */
data object Timeout : AccessError()

/**
 * Thrown when sending the message was cancelled.
 */
data object Cancelled : AccessError()