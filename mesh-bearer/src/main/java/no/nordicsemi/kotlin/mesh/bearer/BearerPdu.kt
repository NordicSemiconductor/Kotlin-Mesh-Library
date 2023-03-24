@file:Suppress("unused", "ArrayInDataClass")

package no.nordicsemi.kotlin.mesh.bearer

/**
 * Event emitted when the bearer receives data.
 *
 * @param data   Data that was received.
 * @param type   Type of the data.
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
     */
    data class OnBearerOpen(val bearer: Bearer) : BearerEvent()

    /**
     * Event emitted when the bearer is closed.
     */
    data class OnBearerClosed(val bearer: Bearer, val error: Throwable) : BearerEvent()
}