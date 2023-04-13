@file:Suppress("unused", "ArrayInDataClass")

package no.nordicsemi.kotlin.mesh.bearer

/**
 * Event emitted when the bearer receives data.
 *
 * @param data   Data that was received.
 * @param type   Type of the PDU.
 */
data class BearerPdu(
    val data: ByteArray,
    val type: PduType
)

/**
 * Bearer event emitted by the bearer.
 */
sealed class BearerEvent {

    /**
     * Event emitted when the bearer is opened.
     *
     * @property bearer Bearer implementation sending and receiving mesh PDUs.
     */
    data class OnBearerOpen(val bearer: Bearer) : BearerEvent()

    /**
     * Event emitted when the bearer is closed.
     *
     * @property bearer     Bearer implementation sending and receiving mesh PDUs.
     * @property error      Error that caused the bearer to close.
     */
    data class OnBearerClosed(val bearer: Bearer, val error: Throwable) : BearerEvent()
}