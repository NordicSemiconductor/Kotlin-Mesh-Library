@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.bearer

/**
 * Defines a set of errors that may be thrown by the bearer.
 */
sealed class BearerError : Exception() {
    /**
     * Thrown when the Central Manager is not in ON state.
     */
    data object CentralManagerNotPoweredOn : BearerError()

    /**
     * Thrown when the PDU type is not supported by the bearer.
     */
    data object PduTypeNotSupported : BearerError()

    /**
     * Thrown when the Bearer is not ready to send data.
     */
    data object Closed : BearerError()

    override fun toString() = when (this) {
        CentralManagerNotPoweredOn -> "Central Manager not powered on."
        Closed -> "PDU type not supported."
        PduTypeNotSupported -> "Bearer is closed"
    }
}