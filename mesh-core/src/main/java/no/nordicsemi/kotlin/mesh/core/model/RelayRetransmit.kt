package no.nordicsemi.kotlin.mesh.core.model

/**
 * The retransmit object is used to describe the number of times a message is published and the interval
 * between retransmissions of the published messages.
 *
 * @param count       An integer from 0 to 7 that represents the number of retransmissions for published messages. A value of 0 represents no retransmissions.
 * @param interval    The interval property contains an integer from 50 to 1600, with a resolution of 50, that represents the interval in milliseconds between the transmissions.
 */
data class RelayRetransmit(val count: Int, val interval: Int)
