package no.nordicsemi.kotlin.mesh.core.model

/**
 * The network transmit object represents the parameters of the transmissions of network layer messages
 * originating from a mesh node.
 *
 * @param count 	The count property contains an integer from 1 to 8 that represents the number of transmissions for
 * 					network messages.
 * @param interval	The interval property contains an integer from 10 to 320 that represents the interval in milliseconds
 * 					between the transmissions.
 */
data class NetworkTransmit internal constructor(
    val count: Int,
    val interval: Int
)