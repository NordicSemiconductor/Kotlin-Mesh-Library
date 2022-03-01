package no.nordicsemi.kotlin.mesh.core.model

/**
 * The network transmit object represents the parameters of the transmissions of network layer messages
 * originating from a mesh node.
 *
 * @param count         The count property contains an integer from 1 to 8 that represents the number of transmissions for
 * 					    network messages.
 * @param interval      The interval property contains an integer from 10 to 320 that represents the interval in milliseconds
 * 					    between the transmissions.
 */
data class NetworkTransmit internal constructor(
    val count: Int,
    val interval: Int
) {
    init {
        require(count in MIN_COUNT..MAX_COUNT) { "Count must be a value from $MIN_COUNT to $MAX_COUNT number of transmissions!" }
        require(interval in MIN_INTERVAL..MAX_INTERVAL) { "Interval must be a value from $MIN_INTERVAL to $MAX_INTERVAL milliseconds between transmissions!" }
    }

    companion object {
        const val MIN_COUNT = 1
        const val MAX_COUNT = 8
        const val MIN_INTERVAL = 10
        const val MAX_INTERVAL = 320
    }
}