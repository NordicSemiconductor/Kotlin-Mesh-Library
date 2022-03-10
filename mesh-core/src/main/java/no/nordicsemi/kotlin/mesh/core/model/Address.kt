@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable
import no.nordicsemi.kotlin.mesh.core.model.serialization.*
import no.nordicsemi.kotlin.mesh.crypto.Crypto
import java.util.*

/**
 * Type alias for an unsigned 16-bit address.
 */
typealias Address = UShort

/**
 * Converts an Address to Hex
 */
fun Address.toHex() = "%04X".format(this)

internal const val minUnicastAddress: Address = 0x0001u
internal const val maxUnicastAddress: Address = 0x7FFFu

//TODO is this really needed?
private const val minVirtualAddress: Address = 0x8000u
private const val maxVirtualAddress: Address = 0xBFFFu

internal const val minGroupAddress: Address = 0xC000u
private const val maxGroupAddress: Address = 0xFEFFu

//TODO is this really needed?
private const val unassignedAddress: Address = 0x0000u
private const val allProxies: Address = 0xFFFCu
private const val allFriends: Address = 0xFFFDu
private const val allRelays: Address = 0xFFFEu
internal const val allNodes: Address = 0xFFFFu

/**
 * Wrapper class for [Address].
 *
 * @property address Unsigned 16-bit [Address].
 */
@Serializable
sealed class MeshAddress {
    abstract val address: Address
}

/**
 * An unassigned address is an address in which the element of a node has not been configured yet or no address has been allocated.
 * The unassigned address has the value 0x0000.
 */
@Serializable
object UnassignedAddress : MeshAddress(),
    HeartbeatPublicationDestination, HeartbeatSubscriptionSource, HeartbeatSubscriptionDestination {
    override val address = unassignedAddress
}

/**
 * A unicast address is a unique address allocated to each element. A unicast address has bit 15 set to 0. The unicast address
 * shall not have the value 0x0000, and therefore can have any value from 0x0001 to 0x7FFF inclusive.
 *
 * @property address Unsigned 16-bit [Address].
 */
@Serializable
data class UnicastAddress(
    override val address: Address
) : MeshAddress(),
    PublicationAddress,
    HeartbeatPublicationDestination,
    HeartbeatSubscriptionSource,
    HeartbeatSubscriptionDestination {
    init {
        require(isInRange(address)) { "A valid unicast address must range from 0x0001 to 0x7FFF!" }
    }

    companion object {
        internal fun isInRange(unicastAddress: Address) =
            unicastAddress in minUnicastAddress..maxUnicastAddress
    }
}

/**
 * A virtual address represents a set of destination addresses. Each virtual address logically represents a Label UUID, which is a 128-bit
 * value that does not have to be managed centrally. One or more elements may be programmed to publish or subscribe to a Label UUID.
 * The Label UUID is not transmitted and shall be used as the Additional Data field of the message integrity check value in the upper transport layer.
 */
@Serializable
data class VirtualAddress(
    @Serializable(with = UUIDSerializer::class) val uuid: UUID
) : MeshAddress(),
    PublicationAddress,
    SubscriptionAddress {
    override val address: Address = Crypto.createVirtualAddress(uuid)
}

/**
 * A group address is an address that is programmed into zero or more elements. A group address has bit 15 set to 1 and bit 14 set to 1.
 * Group addresses in the range 0xFF00 through 0xFFFF are reserved for [FixedGroupAddress], and addresses in the range
 * 0xC000 through 0xFEFF are generally available for other usage.
 *
 * @property address Unsigned 16-bit [Address].
 */
@Serializable
data class GroupAddress(
    override val address: Address
) : MeshAddress(),
    PublicationAddress,
    SubscriptionAddress,
    HeartbeatPublicationDestination,
    HeartbeatSubscriptionDestination {
    init {
        require(isInRange(address)) { "A valid group address must range from 0xC000 to 0xFEFF!" }
    }

    companion object {
        internal fun isInRange(groupAddress: Address) =
            groupAddress in minGroupAddress..maxGroupAddress
    }
}

/**
 * There are two types of group address; those that can be assigned dynamically and those that are fixed.
 * Fixed group addresses are in the range of 0xFF00 through 0xFFFF.
 *
 * @property address Unsigned 16-bit [Address].
 */
sealed class FixedGroupAddress private constructor(override val address: Address) :
    MeshAddress()

/** A message sent to the all-proxies address shall be processed by the primary element of all nodes that have the proxy functionality enabled. */
object AllProxies : FixedGroupAddress(address = allProxies), SubscriptionAddress

/** A message sent to the all-friends address shall be processed by the primary element of all nodes that have the friend functionality enabled. */
object AllFriends : FixedGroupAddress(address = allFriends), SubscriptionAddress

/** A message sent to the all-relays address shall be processed by the primary element of all nodes that have the relay functionality enabled. */
object AllRelays : FixedGroupAddress(address = allRelays), SubscriptionAddress

/** A message sent to the all-nodes address shall be processed by the primary element of all nodes. */
object AllNodes : FixedGroupAddress(address = allNodes)

/** Heartbeat publication destination address for heartbeat messages. This represents a [UnicastAddress], [GroupAddress] or an [UnicastAddress]. */
sealed interface HeartbeatPublicationDestination

/** Heartbeat subscription source address for heartbeat messages. This represents a [UnicastAddress] or an [UnassignedAddress]. */
sealed interface HeartbeatSubscriptionSource

/** Heartbeat subscription destination address for heartbeat messages. This represents a [UnicastAddress], [GroupAddress] or an [UnassignedAddress]. */
sealed interface HeartbeatSubscriptionDestination

/** Publication address a model may subscribe to. This represents a [UnicastAddress], [GroupAddress] or a [VirtualAddress]. */
sealed interface PublicationAddress

/** Subscription address a model may subscribe to. This represents a [GroupAddress], [VirtualAddress], [AllProxies], [AllFriends] or an [AllRelays] address. */
sealed interface SubscriptionAddress