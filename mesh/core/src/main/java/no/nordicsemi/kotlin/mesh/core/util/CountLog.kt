package no.nordicsemi.kotlin.mesh.core.util

import kotlin.math.pow


typealias CountLog = UByte

/**
 * This enum represents number of periodic Heartbeat messages remaining to be sent.
 */
sealed class RemainingHeartbeatPublicationCount {
    /**
     * Periodic Heartbeat messages are not published.
     */
    data object Disabled : RemainingHeartbeatPublicationCount()

    /**
     * Periodic Heartbeat messages are not published.
     */
    data object Indefinitely : RemainingHeartbeatPublicationCount()

    /**
     * Periodic Heartbeat messages are not published.
     *
     * @property value
     *
     */
    data class Exact(val value: UShort) : RemainingHeartbeatPublicationCount()

    /**
     * Remaining count of periodic Heartbeat messages represented as range. Exact count is only
     * available when the count goes down to 2 and 1; otherwise a range is returned.
     *
     * @property low   Short range value.
     * @property high  High range value.
     * @constructor Constructs a range of remaining count of periodic Heartbeat messages.
     */
    data class Range(val low: UShort, val high: UShort) : RemainingHeartbeatPublicationCount()

    /**
     * Periodic Heartbeat messages are not published.
     *
     * @property countLog Count log values sent.
     */
    data class Invalid(val countLog: UByte) : RemainingHeartbeatPublicationCount()
}

/**
 * This enum represents the number of Heartbeat messages received.
 */
sealed class HeartbeatSubscriptionCount {
    /**
     * Number of Heartbeat messages received. Exact count is only available when there was none, or
     * only one Heartbeat message received.
     *
     * @property value Exact count value.
     */
    data class Exact(val value: UShort) : HeartbeatSubscriptionCount()

    /**
     * Number of Heartbeat messages received as range.
     *
     * @property low Start of the range.
     * @property high   End of the range.
     */
    data class Range(val low: UShort, val high: UShort) : HeartbeatSubscriptionCount()

    /**
     * More than 0xFFFE messages have been received.
     */
    data object ReallyALot : HeartbeatSubscriptionCount()

    /**
     * Unsupported CountLog value sent.
     *
     * @property countLog CountLog value sent.
     */
    data class Invalid(val countLog: UByte) : HeartbeatSubscriptionCount()
}


/**
 * Converts the given CountLog value to a RemainingHeartbeatPublicationCount.
 *
 * @return RemainingHeartbeatPublicationCount
 */
internal fun CountLog.toRemainingPublicationCount(): RemainingHeartbeatPublicationCount {
    return when {
        this == 0x00.toUByte() -> RemainingHeartbeatPublicationCount.Disabled
        this == 0xFF.toUByte() -> RemainingHeartbeatPublicationCount.Indefinitely
        this == 0x01.toUByte() || this == 0x02.toUByte() -> RemainingHeartbeatPublicationCount.Exact(
            value = this.toUShort()
        )

        this == 0x11.toUByte() -> RemainingHeartbeatPublicationCount.Range(
            low = 0x8001.toUShort(),
            high = 0xFFFE.toUShort()
        )

        this >= 0x03.toUByte() && this <= 0x10.toUByte() -> RemainingHeartbeatPublicationCount.Range(
            low = ((2.0.pow(this.toDouble() - 2.0)) + 1.0).toUInt().toUShort(),
            high = (2.0.pow(this.toDouble() - 1.0)).toUInt().toUShort()
        )

        else -> RemainingHeartbeatPublicationCount.Invalid(countLog = this)
    }
}

/**
 * Coverts the given CountLog value to HeartbeatSubscriptionCount.
 *
 * @return HeartbeatSubscriptionCount
 */
fun CountLog.toHeartbeatSubscriptionCount(): HeartbeatSubscriptionCount = when {
    this == 0x00.toUByte() || this == 0x01.toUByte() -> HeartbeatSubscriptionCount.Exact(
        value = this.toUShort()
    )

    this == 0xFF.toUByte() || this == 0x011.toUByte() -> HeartbeatSubscriptionCount.ReallyALot
    this >= 0x02.toUByte() && this <= 0x10.toUByte() -> HeartbeatSubscriptionCount.Range(
        low = (2.0.pow(this.toDouble() - 1.0)).toUInt().toUShort(),
        high = minOf(0xFFFEu, (2.0.pow(this.toDouble() - 1.0)).toUInt().toUShort())
    )

    else -> HeartbeatSubscriptionCount.Invalid(countLog = this)
}