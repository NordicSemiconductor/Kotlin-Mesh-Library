package no.nordicsemi.kotlin.mesh.core.util

import no.nordicsemi.kotlin.mesh.core.messages.HeartbeatSubscriptionCount
import no.nordicsemi.kotlin.mesh.core.messages.HeartbeatSubscriptionCount.Exact
import no.nordicsemi.kotlin.mesh.core.messages.HeartbeatSubscriptionCount.Invalid
import no.nordicsemi.kotlin.mesh.core.messages.HeartbeatSubscriptionCount.Range
import no.nordicsemi.kotlin.mesh.core.messages.HeartbeatSubscriptionCount.ReallyALot
import no.nordicsemi.kotlin.mesh.core.messages.RemainingHeartbeatPublicationCount
import kotlin.math.pow


typealias CountLog = UByte


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
    this == 0x00.toUByte() || this == 0x01.toUByte() -> Exact(
        value = this.toUShort()
    )

    this == 0xFF.toUByte() || this == 0x011.toUByte() -> ReallyALot
    this >= 0x02.toUByte() && this <= 0x10.toUByte() -> Range(
        low = (2.0.pow(this.toDouble() - 1.0)).toUInt().toUShort(),
        high = minOf(0xFFFEu, (2.0.pow(this.toDouble() - 1.0)).toUInt().toUShort())
    )

    else -> Invalid(countLog = this)
}