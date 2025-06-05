@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNetworkTransmitSet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNetworkTransmitStatus

/**
 * The network transmit object represents the parameters of the transmissions of network layer
 * messages originating from a mesh node.
 *
 * @param count         The count property contains an integer from 1 to 8 that represents the
 *                      number of transmissions for network messages.
 * @param interval      The interval property contains an integer from 10 to 320 that represents the
 *                      interval in milliseconds between the transmissions.
 */
@Serializable
data class NetworkTransmit internal constructor(
    val count: UByte,
    val interval: UShort
) {
    @Transient
    var steps = toSteps(interval = interval)
        internal set

    val intervalAsSeconds : Double
        get() = interval.toInt() / 1000.0

    val intervalAsMilliseconds : Long
        get() = interval.toInt() * 1000L

    /**
     * Convenience constructor.
     *
     * @param request Network transmit settings received from a node.
     */
    internal constructor(request : ConfigNetworkTransmitSet) : this(
        count = (request.count + 1u).toUByte(),
        interval = ((request.steps + 1u).toUShort() * 10u).toUShort()
    )

    /**
     * Convenience constructor.
     *
     * @param status Network transmit status received from the node.
     */
    internal constructor(status: ConfigNetworkTransmitStatus) : this(
        count = (status.count + 1u).toUByte(),
        interval = ((status.steps + 1u).toUShort() * 10u).toUShort()
    )

    init {
        require(count.toInt() in MIN_COUNT..MAX_COUNT) {
            "Error while creating NetworkTransmit: count must be a value from " +
                    "$MIN_COUNT to $MAX_COUNT number of transmissions!"
        }
        require(interval.toInt() in MIN_INTERVAL..MAX_INTERVAL) {
            "Error while creating NetworkTransmit: interval must be a value from " +
                    "$MIN_INTERVAL to $MAX_INTERVAL milliseconds between transmissions!"
        }
    }

    companion object {
        private const val MIN_COUNT = 1
        private const val MAX_COUNT = 8
        private const val MIN_INTERVAL = 10
        private const val MAX_INTERVAL = 320

        /**
         * Converts Interval to steps.
         *
         * @param interval Interval in milliseconds between the transmissions.
         */
        fun toSteps(interval: UShort): UByte = ((interval.toInt() / 10) - 1).toUByte()
    }
}