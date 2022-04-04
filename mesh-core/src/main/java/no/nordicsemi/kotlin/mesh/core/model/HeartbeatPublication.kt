@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable
import kotlin.math.log2
import kotlin.math.pow

/**
 * The heartbeat publication represents parameters that define the sending of periodic Heartbeat
 * transport control messages.
 *
 * @property address        [HeartbeatPublicationDestination] for heartbeat messages. Destination could be a [UnicastAddress],
 *                          [GroupAddress] or an [UnassignedAddress].Setting an unassigned address as destination will stop
 *                          sending heartbeat messages.
 * @property countLog       The Heartbeat Publication Count Log is a representation of the Heartbeat Publication Count value.
 * @property periodLog      The Heartbeat Publication Period Log state is an 8-bit value that controls the cadence of periodical
 *                          Heartbeat transport control messages. The value is represented as 2^(n-1) seconds.
 * @property ttl            An integer from 0 to 127 that represents the Time to Live (TTL) value for the heartbeat messages.
 * @property index          Represents a [NetworkKey] with the given index.
 * @property features       The functionality of nodes is determined by the [Features] that they support. All nodes have the ability
 *                          to transmit and receive mesh messages. Nodes can also optionally support one or more additional
 *                          features such as [Relay]
 * @property period         An integer from 0 to 65536 that represents the cadence of periodical heartbeat messages in seconds.
 * @property count          Number of Heartbeat messages, 2^(n-1), that remain to be sent.
 */
@Serializable
data class HeartbeatPublication internal constructor(
    val address: HeartbeatPublicationDestination,
    private var _countLog: UByte,
    val periodLog: UByte,
    val ttl: UByte,
    val index: Int,
    val features: Array<Feature>
) {
    val period: UShort by lazy {
        2.toDouble().pow(periodLog.toInt() - 1).toInt().toUShort()
    }

    var countLog: UByte
        get() = _countLog
        internal set(value) {
            _countLog = value
        }

    val count: UShort by lazy {
        2.toDouble().pow(_countLog.toInt() - 1).toInt().toUShort()
    }

    internal constructor(
        address: HeartbeatPublicationDestination,
        period: UShort,
        ttl: UByte,
        index: Int,
        features: Array<Feature>
    ) : this(
        address = address,
        _countLog = 0x00.toUByte(),
        periodLog = (log2(period.toDouble()).toInt() + 1).toUByte(),
        ttl = ttl.dec(),
        index = index,
        features = features
    ) {
        require(period.toInt() in MIN_PERIOD..MAX_PERIOD) { "Period must range from $MIN_PERIOD to $MAX_PERIOD!" }
        require(ttl.toInt() in MIN_TTL..MAX_TTL) { "TTL must range from $MIN_TTL to $MAX_TTL!" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HeartbeatPublication

        if (address != other.address) return false
        if (_countLog != other._countLog) return false
        if (periodLog != other.periodLog) return false
        if (ttl != other.ttl) return false
        if (index != other.index) return false
        if (!features.contentEquals(other.features)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = address.hashCode()
        result = 31 * result + _countLog.hashCode()
        result = 31 * result + periodLog.hashCode()
        result = 31 * result + ttl.hashCode()
        result = 31 * result + index
        result = 31 * result + features.contentHashCode()
        return result
    }

}

private const val MIN_PERIOD_LOG = 0x01
private const val MAX_PERIOD_LOG = 0x11

private const val MIN_PERIOD = 0
private const val MAX_PERIOD = 65536

private const val MIN_TTL = 0
private const val MAX_TTL = 127