@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.bearer

/**
 * Defines a set of errors that may be thrown by the bearer.
 */
sealed class BearerError : Exception() {
    /**
     * Thrown when the Central Manager is not in ON state.
     */
    data object CentralManagerNotPoweredOn : BearerError() {
        private fun readResolve(): Any = CentralManagerNotPoweredOn

        override fun toString() = "Central Manager not powered on."
    }

    /**
     * Thrown when the PDU type is not supported by the bearer.
     */
    data object PduTypeNotSupported : BearerError() {
        private fun readResolve(): Any = PduTypeNotSupported

        override fun toString() = "PDU type not supported."
    }

    /**
     * Thrown when the Bearer is not ready to send data.
     */
    data object Closed : BearerError() {
        private fun readResolve(): Any = Closed

        override fun toString() = "Bearer is closed."
    }
}