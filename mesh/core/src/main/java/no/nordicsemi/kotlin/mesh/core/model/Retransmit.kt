@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable
import no.nordicsemi.kotlin.mesh.core.model.serialization.DurationToIntSerializer
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * The retransmit object is used to describe the number of times a message is published and the
 * interval between retransmissions of the published messages.
 *
 * @property count          An integer from 0 to 7 that represents the number of retransmissions for
 *                          published messages. A value of 0 represents no retransmissions.
 * @property interval       The interval property contains an integer from 50 to 1600, with a
 *                          resolution of 50, that represents the interval in milliseconds between
 *                          the transmissions.
 */
@Serializable
data class Retransmit(
    val count: UByte,
    @Serializable(with = DurationToIntSerializer::class)
    val interval: Duration
) {

    /**
     * Creates the Retransmit object.
     *
     * @param count         Number of retransmissions for network messages.
     *                      The value is in range from 0 to 7, where 0 means no retransmissions.
     * @param intervalSteps Retransmission steps, from 0 to 31. Each step adds 50 ms to initial
     *                      50 ms interval.
     */
    constructor(
        count: UByte,
        intervalSteps: UByte
    ) : this(
        count = count,
        interval = ((intervalSteps.toLong() + 1L) * INTERVAL_STEP)
            .toDuration(DurationUnit.MILLISECONDS)
    )

    init {
        require(count in MIN_RETRANSMIT_COUNT..MAX_RETRANSMIT_COUNT) {
            "Invalid count value in Retransmit"
        }
        require(
            interval in MIN_INTERVAL..MAX_INTERVAL &&
                    interval.inWholeMilliseconds % INTERVAL_STEP == 0L
        ) {
            "Invalid interval value"
        }
    }

    /**
     * Retransmission steps, from 0 to 31. Use `interval` to get the interval in ms.
     */
    val steps: UByte by lazy { (interval.inWholeMilliseconds / 50 - 1).toUByte() }

    companion object {
        const val MIN_RETRANSMIT_COUNT: UByte = 0u
        const val MAX_RETRANSMIT_COUNT: UByte = 7u
        private const val INTERVAL_STEP = 50L
        val MIN_INTERVAL = 50.toDuration(DurationUnit.MILLISECONDS)
        val MAX_INTERVAL = 1600.toDuration(DurationUnit.MILLISECONDS)

        /**
         * Creates the Retransmit object when there should be no retransmissions.
         */
        val disabled = Retransmit(count = MIN_RETRANSMIT_COUNT, interval = MIN_INTERVAL)
    }
}