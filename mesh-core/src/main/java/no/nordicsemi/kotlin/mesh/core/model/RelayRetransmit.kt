package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable

/**
 * The retransmit object is used to describe the number of times a message is published and the interval
 * between retransmissions of the published messages.
 *
 * @property count       An integer from 1 to 8 that represents the number of transmissions for relay messages
 * @property interval    an integer from 10 to 320 that represents the interval in milliseconds between the transmissions.
 */
@Serializable
data class RelayRetransmit internal constructor(val count: Int, val interval: Int) {
    init {
        require(count in 1..8) {"Count must be a from 1 to 8"}
        require(interval in 10..320) {"Interval must range from 10 to 320"}
    }
}
