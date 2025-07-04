@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.bearer

/**
 * Defines a set of errors that may be thrown by the bearer.
 */
sealed class BearerError : Exception() {
    /**
     * Thrown when the Central Manager is not in ON state.
     */
    class CentralManagerNotPoweredOn : BearerError() {
        override fun toString() = "Central Manager not powered on."
    }

    /**
     * Thrown when the PDU type is not supported by the bearer.
     */
    class PduTypeNotSupported : BearerError() {
        override fun toString() = "PDU type not supported."
    }

    /**
     * Thrown when the Bearer is not ready to send data.
     */
    class Closed : BearerError() {
        override fun toString() = "Bearer is closed."
    }
}