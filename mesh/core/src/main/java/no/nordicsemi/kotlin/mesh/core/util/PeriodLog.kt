package no.nordicsemi.kotlin.mesh.core.util

import kotlin.math.pow


typealias PeriodLog = UByte

/**
 *This enum represents remaining period for processing Heartbeat messages, in seconds.
 */
sealed class RemainingHeartbeatSubscriptionPeriod {

    /**
     * Heartbeat messages are not processed.
     */
    data object Disabled : RemainingHeartbeatSubscriptionPeriod()

    /**
     * Exact remaining period for processing Heartbeat messages, in seconds. Exact period is only
     * available when the count goes down to 1 or when is maximum; otherwise a range is returned.
     *
     * @property value Exact period value.
     */
    data class Exact(val value: UShort) : RemainingHeartbeatSubscriptionPeriod()

    /**
     * Remaining period for processing Heartbeat messages as range, in seconds.
     *
     * @property range Remaining period range.
     */
    data class Range(val low: UShort, val high: UShort) : RemainingHeartbeatSubscriptionPeriod()

    /**
     * Unsupported PeriodLog value sent.
     *
     * @property periodLog PeriodLog value sent.
     */
    data class Invalid(val periodLog: UByte) : RemainingHeartbeatSubscriptionPeriod()
}

internal fun PeriodLog.toRemainingHeartbeatSubscriptionPeriod() = when {
    this == 0x00.toUByte() -> RemainingHeartbeatSubscriptionPeriod.Disabled
    this == 0x01.toUByte() -> RemainingHeartbeatSubscriptionPeriod.Exact(value = 1u)
    this == 0x11.toUByte() -> RemainingHeartbeatSubscriptionPeriod.Exact(value = 0xFFFFu)
    this >= 0x02.toUByte() && this <= 0x11.toUByte() -> RemainingHeartbeatSubscriptionPeriod.Range(
        low = 2.0.pow(this.toDouble() - 1).toUInt().toUShort(),
        high = (2.0.pow(this.toDouble() - 1).toUInt().toUShort() - 1u).toUShort()
    )

    else -> RemainingHeartbeatSubscriptionPeriod.Invalid(periodLog = this)
}