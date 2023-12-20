package no.nordicsemi.kotlin.mesh.core.layers.lowertransport

/**
 * Defines a set of errors originating from the lower transport layer.
 */
sealed class LowerTransportError : Exception() {
    /**
     * Thrown when the segmented message has not been acknowledged before the timeout occurred.
     */
    data object Timeout : LowerTransportError()

    /**
     * Sending segmented messages was cancelled.
     */
    data object Cancelled : LowerTransportError()

    /**
     * Thrown when the target device is busy at the moment and could not accept the message.
     */
    data object Busy : LowerTransportError()

    override fun toString() = "LowerTransport: ${
        when (this) {
            Timeout -> "Request timed out in Lower Transport layer."
            Cancelled -> "Message cancelled ."
            Busy -> "Node is busy, Try later"
        }
    }"
}
