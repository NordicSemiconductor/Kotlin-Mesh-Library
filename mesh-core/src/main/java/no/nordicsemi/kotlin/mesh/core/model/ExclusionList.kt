@file:Suppress("MemberVisibilityCanBePrivate")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable

/**
 * Represents the unicast addresses that are excluded by a Mesh Manager for a particular IV index.
 *
 * @property ivIndex       32-bit value that is a shared network resource known by all nodes in a given network.
 * @property addresses     List of excluded addresses for a given ivIndex.
 */
@Serializable
data class ExclusionList internal constructor(val ivIndex: UInt) {
    var addresses = listOf<UnicastAddress>()
        private set

    internal var network: MeshNetwork? = null

    /**
     * Excludes a given unicast address.
     *
     * @param address Unicast address to be excluded.
     */
    fun exclude(address: UnicastAddress): Boolean {
        if (address !in addresses) {
            addresses = addresses + address
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
    fun isExcluded(address: UnicastAddress): Boolean = address in addresses
}
