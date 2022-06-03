@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.datetime.Instant

/**
 * The IV Index received with the last Secure Network Beacon and its
 * current state.
 *
 * Bluetooth Mesh Profile Specification 1.0.1, Chapter 3.10.5:
 *
 * During the Normal Operation state, the IV Update Flag in the Secure Network beacon and in the
 * Friend Update message shall be set to 0. When this state is active, a node shall transmit using
 * the current IV Index and shall process messages from the current IV Index and also the current
 * IV Index - 1.
 *
 * During the IV Update in Progress state, the IV Update Flag in the Secure Network beacon and in
 * the Friend Update message shall be set to 1. When this state is active, a node shall transmit
 * using the current IV Index - 1 and shall process messages from the current IV Index - 1 and also
 * the current IV Index.
 *
 * @property index           A 32-bit shared network resource. All nodes in a mesh network share
 *                             the same value of the IV Index and use it for all subnets they belong
 *                             to.
 * @property isIvUpdateActive  Set to true if IV Update is in progress and false if the network is
 *                             in normal operation.
 * @property transitionDate    Time when the last iv update happened.
 * @property ivRecoveryFlag    Represents the current iv recovery procedure state. A node that is
 *                             away from the network for a long time may miss IV Update procedures,
 *                             in which case it can no longer communicate with the other nodes. In
 *                             order to recover the IV Index, the node must listen for a Secure
 *                             Network beacon, which contains the Network ID and the current
 *                             IV Index. Upon receiving and successfully authenticating a Secure
 *                             Network beacon for a primary subnet whose IV Index is 1 or more
 *                             higher than the current known IV Index, the node shall set its
 *                             current IV Index and its current IV Update procedure state from the
 *                             values in this Secure Network beacon.
 * @property transmitIvIndex   IV index used when transmitting messages.
 */
data class IvIndex internal constructor(
    val index: UInt = 0u,
    val isIvUpdateActive: Boolean = false,
    val transitionDate: Instant = Instant.fromEpochMilliseconds(System.currentTimeMillis())
) {
    var ivRecoveryFlag = false
        internal set

    val transmitIvIndex: UInt
        get() = when (isIvUpdateActive && index > 0u) {
            true -> index - 1u
            false -> index
        }
}