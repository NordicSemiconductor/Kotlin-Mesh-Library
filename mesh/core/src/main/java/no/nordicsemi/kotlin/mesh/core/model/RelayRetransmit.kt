package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable

/**
 * The relay retransmit object represents the parameters of the retransmissions of network layer
 * messages relayed by a mesh node.
 *
 * @property count           An integer from 1 to 8 that represents the number of transmissions for
 *                           relay messages.
 * @property interval        An integer from 10 to 320 that represents the interval in milliseconds
 *                           between the transmissions.
 * @property steps           Number of steps between each retransmission (10 to 320 ms in 10ms steps).
 * @property timeInterval    The time interval between each transmissions in seconds.
 */
@Serializable
data class RelayRetransmit internal constructor(val count: Int, val interval: Int) {

    val steps: UByte
        get() = ((interval / 10) - 1).toUByte()

    val timeInterval: Duration
        get() = interval.toDuration(DurationUnit.SECONDS)

    init {
        require(count in MIN_COUNT..MAX_COUNT) {
            "Count must be a from $MIN_COUNT to $MAX_COUNT"
        }
        require(interval in MIN_INTERVAL..MAX_INTERVAL) {
            "Interval must be from $MIN_INTERVAL to $MAX_INTERVAL"
        }
    }

    internal companion object {
        const val MIN_COUNT = 1
        const val MAX_COUNT = 8
        const val MIN_INTERVAL = 10
        const val MAX_INTERVAL = 320
    }
}
