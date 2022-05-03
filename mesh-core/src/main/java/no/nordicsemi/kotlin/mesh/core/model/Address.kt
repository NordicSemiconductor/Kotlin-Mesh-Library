@file:Suppress("unused", "SERIALIZER_TYPE_INCOMPATIBLE")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable
import no.nordicsemi.kotlin.mesh.core.model.serialization.MeshAddressSerializer
import no.nordicsemi.kotlin.mesh.crypto.Crypto
import java.util.*

/**
 * Type alias for an unsigned 16-bit address.
 */
typealias Address = UShort

/**
 * Represents a hex representation of an Address in String,
 */
fun Address.toHex(prefix0x: Boolean = false) = "%04X".format(this.toShort()).run {
    when {
        prefix0x -> "0x$this"
        else -> this
    }.uppercase()
}

internal const val minUnicastAddress: Address = 0x0001u
internal const val maxUnicastAddress: Address = 0x7FFFu

//TODO is this really needed?
private const val minVirtualAddress: Address = 0x8000u
private const val maxVirtualAddress: Address = 0xBFFFu

internal const val minGroupAddress: Address = 0xC000u
private const val maxGroupAddress: Address = 0xFEFFu

//TODO is this really needed?
internal const val unassignedAddress: Address = 0x0000u
private const val allProxies: Address = 0xFFFCu
private const val allFriends: Address = 0xFFFDu
private const val allRelays: Address = 0xFFFEu
internal const val allNodes: Address = 0xFFFFu

/**
 * An interface containing a property type address.
 *
 * @property address Unsigned 16-bit [Address].
 */
sealed interface HasAddress {
    val address: Address
}

/**
 * Wrapper class for [Address].
 */
@Serializable(with = MeshAddressSerializer::class)
sealed class MeshAddress : HasAddress {
    abstract override val address: Address

    companion object {

        /**
         * Creates a Mesh address of type Unassigned, Unicast or Group address using the given
         * address value.
         *
         * @param address Address value.
         * @throws IllegalArgumentException If the given address value is not a valid Unassigned,
         *                                  Unicast or a Group address.
         */
        fun create(address: Address): MeshAddress = when {
            UnassignedAddress.isValid(address = address) -> UnassignedAddress
            UnicastAddress.isValid(address = address) -> UnicastAddress(address = address)
            GroupAddress.isValid(address = address) -> GroupAddress(address = address)
            else -> throw IllegalArgumentException(
                "Unable to create an Address for the given address value!"
            )
        }

        /**
         * Creates a virtual address using the given UUID Label.
         *
         * @param uuid UUID Label.
         */
        fun create(uuid: UUID) = VirtualAddress(uuid = uuid)
    }
}

/**
 * The unassigned address has the value 0x0000.
 */
@Serializable(with = MeshAddressSerializer::class)
object UnassignedAddress : MeshAddress(),
        ParentGroupAddress,
        PublicationAddress,
        SubscriptionAddress,
        HeartbeatPublicationDestination,
        HeartbeatSubscriptionDestination {
    override val address = unassignedAddress

    fun isValid(address: Address): Boolean = address == unassignedAddress
}

/**
 * A unicast address is a unique address allocated to each element. A unicast address has bit 15 set
 * to 0. The unicast address shall not have the value 0x0000, and therefore can have any value from
 * 0x0001 to 0x7FFF inclusive.
 */
@Serializable(with = MeshAddressSerializer::class)
data class UnicastAddress(
    override val address: Address
) : MeshAddress(),
        PublicationAddress,
        HeartbeatPublicationDestination,
        HeartbeatSubscriptionSource,
        HeartbeatSubscriptionDestination {
    init {
        require(isValid(address)) {
            "A valid unicast address must range from $minUnicastAddress to $maxUnicastAddress!"
        }
    }

    operator fun plus(other: Int) = UnicastAddress((address.toInt() + other).toUShort())

    operator fun minus(other: Int) = UnicastAddress((address.toInt() - other).toUShort())

    operator fun compareTo(o: UnicastAddress) = address.toInt().compareTo(o.address.toInt())

    operator fun rangeTo(o: UnicastAddress) = UnicastRange(this, o)

    companion object {
        fun isValid(address: Address) = address in minUnicastAddress..maxUnicastAddress
    }
}

/**
 * A virtual address represents a set of destination addresses. Each virtual address logically
 * represents a Label UUID,
 * which is a 128-bit value that does not have to be managed centrally. One or more elements may be
 * programmed to publish or subscribe to a Label UUID. The Label UUID is not transmitted and shall
 * be used as the Additional Data field of the message integrity check value in the upper transport
 * layer.
 */
@Serializable(with = MeshAddressSerializer::class)
data class VirtualAddress(
    val uuid: UUID
) : MeshAddress(),
        PrimaryGroupAddress,
        ParentGroupAddress,
        PublicationAddress,
        SubscriptionAddress {
    override val address: Address = Crypto.createVirtualAddress(uuid)

    operator fun compareTo(o: VirtualAddress) = address.toInt().compareTo(o.address.toInt())
}

/**
 * A group address is an address that is programmed into zero or more elements. A group address has
 * bit 15 set to 1 and bit 14 set to 1. Group addresses in the range 0xFF00 through 0xFFFF are
 * reserved for [FixedGroupAddress], and addresses in the range 0xC000 through 0xFEFF are generally
 * available for other usage.
 */
@Serializable(with = MeshAddressSerializer::class)
data class GroupAddress(
    override val address: Address
) : MeshAddress(),
        PrimaryGroupAddress,
        ParentGroupAddress,
        PublicationAddress,
        SubscriptionAddress,
        HeartbeatPublicationDestination,
        HeartbeatSubscriptionDestination {
    init {
        require(isValid(address)) {
            "A valid group address must range from $minGroupAddress to $maxGroupAddress!"
        }
    }

    operator fun plus(o: Int): GroupAddress = GroupAddress((address.toInt() + o).toUShort())

    operator fun minus(other: Int) = GroupAddress((address.toInt() - other).toUShort())

    operator fun compareTo(o: GroupAddress) = address.toInt().compareTo(o.address.toInt())

    operator fun compareTo(o: PrimaryGroupAddress) = address.toInt().compareTo(o.address.toInt())

    operator fun compareTo(o: ParentGroupAddress) = address.toInt().compareTo(o.address.toInt())

    operator fun rangeTo(o: GroupAddress) = GroupRange(this, o)

    companion object {
        fun isValid(address: Address) = address in minGroupAddress..maxGroupAddress
    }
}

/**
 * There are two types of group address; those that can be assigned dynamically and those that are
 * fixed. Fixed group addresses are in the range of 0xFF00 through 0xFFFF.
 */
@Serializable
sealed class FixedGroupAddress(override val address: Address) : MeshAddress()

/**
 * A message sent to the all-proxies address shall be processed by the primary element of all nodes
 * that have the proxy functionality enabled.
 */
object AllProxies : FixedGroupAddress(address = allProxies), SubscriptionAddress

/**
 * A message sent to the all-friends address shall be processed by the primary element of all nodes
 * that have the friend functionality enabled.
 */
object AllFriends : FixedGroupAddress(address = allFriends), SubscriptionAddress

/**
 * A message sent to the all-relays address shall be processed by the primary element of all nodes
 * that have the relay functionality enabled.
 */
object AllRelays : FixedGroupAddress(address = allRelays), SubscriptionAddress

/**
 * A message sent to the all-nodes address shall be processed by the primary element of all nodes.
 *
 * Note: AllNodes cannot be used as subscription address.
 */
object AllNodes : FixedGroupAddress(address = allNodes)

/**
 * Heartbeat publication destination address for heartbeat messages. This represents a
 * [UnicastAddress] or a [GroupAddress].
 */
sealed interface HeartbeatPublicationDestination : HasAddress

/**
 * Heartbeat subscription source address for heartbeat messages. This represents a [UnicastAddress].
 */
sealed interface HeartbeatSubscriptionSource : HasAddress

/**
 * Heartbeat subscription destination address for heartbeat messages. This represents a
 * [UnicastAddress] or a [GroupAddress].
 */
sealed interface HeartbeatSubscriptionDestination : HasAddress

/**
 * An address a model may publish to. This represents a [UnicastAddress], [GroupAddress]
 * or a [VirtualAddress].
 */
@Serializable(with = MeshAddressSerializer::class)
sealed interface PublicationAddress : HasAddress

/**
 * An address a model may subscribe to. This represents a [GroupAddress], [VirtualAddress],
 * [AllProxies], [AllFriends] or an [AllRelays] address.
 */
@Serializable(with = MeshAddressSerializer::class)
sealed interface SubscriptionAddress : HasAddress

/**
 * An address type used to identify a [GroupAddress] or a [VirtualAddress] that's used to create a
 * group. Primary group address cannot be a fixed group address and not allocatable to a provisioner
 * as a range.
 */
@Serializable(with = MeshAddressSerializer::class)
sealed interface PrimaryGroupAddress : HasAddress

/**
 * An address type used to identify a [GroupAddress], [VirtualAddress] or an [UnassignedAddress]
 * that's used as a parent address of a group. Parent group address cannot be a fixed group address
 * and not allocatable to a provisioner as a range.
 */
@Serializable(with = MeshAddressSerializer::class)
sealed interface ParentGroupAddress : HasAddress