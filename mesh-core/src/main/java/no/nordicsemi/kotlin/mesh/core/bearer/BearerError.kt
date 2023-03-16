@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.bearer

/** Set of errors that may be thrown by the bearer. */
sealed class BearerError : Exception()

/** Thrown when the PDU type is not supported by the bearer. */
object PduTypeNotSupported : BearerError()

/** Thrown when the Bearer is not ready to send data. */
object BearerClosed : BearerError()