@file:Suppress("MemberVisibilityCanBePrivate", "PropertyName")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the unicast addresses that are excluded by a Mesh Manager for a particular IV index.
 *
 * @property ivIndex       32-bit value that is a shared network resource known by all nodes in a
 *                         given network.
 * @property addresses     List of excluded addresses for a given ivIndex.
 * @constructor            Creates an ExclusionList object.
 */
@Serializable
data class ExclusionList internal constructor(
    val ivIndex: UInt,
    @SerialName(value = "addresses")
    internal val _addresses: MutableList<UnicastAddress> = mutableListOf()
) {
    val addresses: List<UnicastAddress>
        get() = _addresses

    internal var network: MeshNetwork? = null

    /**
     * Excludes a given unicast address.
     *
     * @param address Unicast address to be excluded.
     */
    fun exclude(address: UnicastAddress): Boolean {
        if (address !in _addresses) {
            _addresses.add(address)
        }
        network?.updateTimestamp()
        return true
    }

    /**
     * Excludes all the unicast addresses of the elements in a given node.
     *
     * @param node Node containing the element addresses to be excluded.
     */
    fun exclude(node: Node) {
        node._elements.forEach { element ->
            exclude(UnicastAddress(address = element.unicastAddress.address))
        }
        network?.updateTimestamp()
    }

    /**
     * Returns true if the address is excluded.
     *
     * @param address unicast address to be verified.
     */
    fun isExcluded(address: UnicastAddress): Boolean = address in _addresses
}

/**
 * Checks whether the given Unicast Address range can be reassigned to a new Node, as it has been
 * used by a Node that was recently removed. Other nodes may still keep the sequence number
 * associated with this address and may discard packets sent from it.
 *
 * @param range Unicast range to check.
 * @param ivIndex Current IV Index .
 * @returns true if the given address is excluded or false otherwise.
 */
fun List<ExclusionList>.contains(
    range: UnicastRange,
    ivIndex: IvIndex
) = isNotEmpty() && excludedAddresses(ivIndex).any { range.contains(it.address) }

/**
 * Returns a list of Unicast addresses for hte given IV Index.
 * @param ivIndex IV Index of the exclusion list.
 *
 */
fun List<ExclusionList>.excludedAddresses(ivIndex: IvIndex) = filter {
    it.ivIndex == ivIndex.index || (ivIndex.index > 0u && it.ivIndex == ivIndex.index - 1u)
}.flatMap { it.addresses }