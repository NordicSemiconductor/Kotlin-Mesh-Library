package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigRelaySet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigRelayStatus
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

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
data class RelayRetransmit(val count: Int, val interval: Int) {

    val steps: UByte
        get() = ((interval / 10) - 1).toUByte()

    val timeInterval: Duration
        get() = interval.toDuration(DurationUnit.SECONDS)

    init {
        require(count in MIN_COUNT..MAX_COUNT) {
            "Error while creating RelayRetransmit: count value was $count. Count must range from " +
                    "$MIN_COUNT to $MAX_COUNT"
        }
        require(interval in MIN_INTERVAL..MAX_INTERVAL) {
            "Error while creating RelayRetransmit: interval value was $interval. Interval must range" +
                    " from $MIN_INTERVAL to $MAX_INTERVAL"
        }
    }

    /**
     * Convenience constructor
     *
     * @param incorrectRetransmit incorrect relay retransmit
     */
    @Suppress("unused")
    internal constructor(incorrectRetransmit: RelayRetransmit) : this(
        count = incorrectRetransmit.count,
        interval = incorrectRetransmit.interval
    )

    /**
     * Convenience constructor to be invoked upon receiving a [ConfigRelaySet] message.
     *
     * @param request [ConfigRelaySet] message.
     */
    @Suppress("unused")
    internal constructor(request: ConfigRelaySet) : this(
        count = request.count + 1,
        interval = (request.steps.toInt() + 1) * 10
    )

    /**
     * Convenience constructor to be invoked upon receiving a [ConfigRelayStatus] message.
     *
     * @param status [ConfigRelayStatus] message.
     */
    internal constructor(status: ConfigRelayStatus) : this(
        count = status.count + 1,
        interval = (status.steps.toInt() + 1) * 10
    )

    companion object {
        const val MIN_COUNT = 1
        const val MAX_COUNT = 8
        val COUNT_RANGE = MIN_COUNT..MAX_COUNT
        const val MIN_INTERVAL = 10
        const val MAX_INTERVAL = 320
        val INTERVAL_RANGE = MIN_INTERVAL..MAX_INTERVAL
    }
}
