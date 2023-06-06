@file:Suppress("unused", "ArrayInDataClass")

package no.nordicsemi.kotlin.mesh.bearer

/**
 * Event emitted when the bearer receives data.
 *
 * @param data   Data that was received.
 * @param type   Type of the PDU.
 */
data class BearerPdu(val data: ByteArray, val type: PduType)

/**
 * Bearer event emitted by the bearer.
 */
sealed class BearerEvent {

    /**
     * Event emitted when the bearer is opened.
     */
    object Opened : BearerEvent()

    /**
     * Event emitted when the bearer is closed.
     *
     * @property error Error that caused the bearer to close.
     */
    data class Closed(val error: Throwable) : BearerEvent()
}