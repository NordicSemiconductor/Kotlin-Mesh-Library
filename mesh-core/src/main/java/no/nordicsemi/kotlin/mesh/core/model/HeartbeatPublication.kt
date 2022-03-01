package no.nordicsemi.kotlin.mesh.core.model

/**
 * The heartbeat publication represents parameters that define the sending of periodic Heartbeat
 * transport control messages
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
data class HeartbeatPublication internal constructor(
    val address: HeartbeatPublicationDestination,
    val period: Int,
    val ttl: Int,
    internal val index: Int,
    val features: Array<Feature>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HeartbeatPublication

        if (address != other.address) return false
        if (period != other.period) return false
        if (ttl != other.ttl) return false
        if (index != other.index) return false
        if (!features.contentEquals(other.features)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = address.hashCode()
        result = 31 * result + period
        result = 31 * result + ttl
        result = 31 * result + index
        result = 31 * result + features.contentHashCode()
        return result
    }
}