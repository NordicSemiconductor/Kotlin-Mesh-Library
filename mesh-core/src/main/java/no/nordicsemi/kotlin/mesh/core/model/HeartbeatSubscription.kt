package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The heartbeat subscription object represents parameters that define the receiving of periodical Heartbeat
 * transport control messages
 *
 * @property source         The source property contains the source address for Heartbeat messages that a node processes.
 * @property destination    The destination property represents the destination address for the Heartbeat messages.
 */
@Serializable
data class HeartbeatSubscription internal constructor(
    val source: UnicastAddress,
    val destination: HeartbeatSubscriptionDestination,
)