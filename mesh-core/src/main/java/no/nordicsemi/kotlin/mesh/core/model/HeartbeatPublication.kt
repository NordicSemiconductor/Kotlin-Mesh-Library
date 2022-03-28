@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.math.log2

/**
 * The heartbeat publication represents parameters that define the sending of periodic Heartbeat
 * transport control messages.
 *
 * @property address        [HeartbeatPublicationDestination] for heartbeat messages. Destination could be a [UnicastAddress],
 *                          [GroupAddress] or an [UnassignedAddress].Setting an unassigned address as destination will stop
 *                          sending heartbeat messages.
 * @property period         An integer from 0 to 65536 that represents the cadence of periodical heartbeat messages in seconds.
 * @property ttl            An integer from 0 to 127 that represents the Time to Live (TTL) value for the heartbeat messages.
 * @property index          Represents a [NetworkKey] with the given index.
 * @property features       The functionality of nodes is determined by the [Features] that they support. All nodes have the ability
 *                          to transmit and receive mesh messages. Nodes can also optionally support one or more additional features such as [Relay]
 */
@Serializable
data class HeartbeatPublication internal constructor(
    val address: HeartbeatPublicationDestination,
    val period: UShort,
    val ttl: UByte,
    val index: Int,
    val features: Array<Feature>
) {
    val periodLog: UByte
        get() = (log2(period.toDouble()).toInt() + 1).toUByte()

    @Transient
    var countLog: UByte = 0x00.toUByte()
        internal set

    init {
        require(period.toInt() in MIN_PERIOD..MAX_PERIOD) { "Period must range from $MIN_PERIOD to $MAX_PERIOD!" }
        require(ttl.toInt() in MIN_TTL..MAX_TTL) { "TTL must range from $MIN_TTL to $MAX_TTL!" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HeartbeatPublication

        if (address != other.address) return false
        if (period != other.period) return false
        if (ttl != other.ttl) return false
        if (index != other.index) return false
        if (!features.contentEquals(other.features)) return false
        if (countLog != other.countLog) return false

        return true
    }

    override fun hashCode(): Int {
        var result = address.hashCode()
        result = 31 * result + period.hashCode()
        result = 31 * result + ttl.hashCode()
        result = 31 * result + index
        result = 31 * result + features.contentHashCode()
        result = 31 * result + countLog.hashCode()
        return result
    }
}

private const val MIN_PERIOD = 0
private const val MAX_PERIOD = 65536

private const val MIN_TTL = 0
private const val MAX_TTL = 127