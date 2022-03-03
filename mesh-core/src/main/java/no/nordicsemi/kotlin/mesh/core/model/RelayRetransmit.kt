package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable

/**
 * The retransmit object is used to describe the number of times a message is published and the interval
 * between retransmissions of the published messages.
 *
 * @property count       An integer from 0 to 7 that represents the number of retransmissions for published messages. A value of 0 represents no retransmissions.
 * @property interval    The interval property contains an integer from 50 to 1600, with a resolution of 50, that represents the interval in milliseconds between the transmissions.
 */
@Serializable
data class RelayRetransmit internal constructor(val count: Int, val interval: Int)
