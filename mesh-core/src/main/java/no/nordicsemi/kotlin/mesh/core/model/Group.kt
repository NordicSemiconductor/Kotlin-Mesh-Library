@file:Suppress("MemberVisibilityCanBePrivate")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.properties.Delegates

/**
 * Group defines a [GroupAddress] of type [PrimaryGroupAddress] to which a node may subscribe to.
 *
 * If a node is part of a Group, at least one model of the node is subscribed to the Group’s group
 * address. A Group may have a Parent Group. In this case, all the models of a node that are
 * subscribed to the Group’s address are also subscribed to the Parent Group’s address.
 *
 * For example, the Second-Floor Group is a parent of the Bedroom Group and the Guest Bedroom Group.
 * In this case, at least one model of all the nodes of the Bedroom Group is subscribed to a group
 * address or virtual label of the Bedroom Group and Second-Floor Group; and at least one model of
 * all the nodes of the Guest Bedroom Group is subscribed to the group address or virtual label of
 * the Guest Bedroom Group and the Second-Floor Group. Note library does not validate for cyclic
 * parent-groups.
 *
 * @property name             Group name.
 * @property address          Address of the group.
 * @property parentAddress    Parent address of the group if the given group is a sub group.
 */
@Serializable
data class Group(
    val address: PrimaryGroupAddress,
) {
    var name: String by Delegates.observable(
        initialValue = "nRF Group"
    ) { _, oldValue, newValue ->
        require(newValue.isNotBlank()) {
            "Group name cannot be empty!"
        }
        MeshNetwork.onChange(
            oldValue = oldValue,
            newValue = newValue,
            action = { network?.updateTimestamp() })
    }
    var parentAddress: ParentGroupAddress by Delegates.observable(
        initialValue = UnassignedAddress
    ) { _, oldValue, newValue ->
        require(address.address != parentAddress.address) {
            "Primary group address cannot be the same as the parent group address!"
        }
        MeshNetwork.onChange(
            oldValue = oldValue,
            newValue = newValue,
            action = { network?.updateTimestamp() })
    }

    @Transient
    internal var network: MeshNetwork? = null
}
