@file:Suppress("unused", "ArrayInDataClass")

package no.nordicsemi.kotlin.mesh.bearer

/**
 * Bearer data event emitted by the bearer.
 */
sealed class BearerDataEvent {
    /**
     * Event emitted when the bearer receives data.
     *
     * @param bearer Bearer that received the data.
     * @param data   Data that was received.
     * @param type   Type of the data.
     */
    data class OnBearerReceiveData(
        val bearer: Bearer,
        val data: ByteArray,
        val type: PduType
    ) : BearerDataEvent()
}


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