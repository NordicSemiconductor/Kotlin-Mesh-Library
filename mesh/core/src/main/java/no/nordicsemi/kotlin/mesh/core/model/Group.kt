@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import no.nordicsemi.kotlin.mesh.core.exception.DoesNotBelongToNetwork

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
    @SerialName(value = "name")
    private var _name: String,
    val address: PrimaryGroupAddress,
) {
    var name: String
        get() = _name
        set(value) {
            require(value.isNotBlank()) { "Group name cannot be empty!" }
            MeshNetwork.onChange(oldValue = _name, newValue = value) { network?.updateTimestamp() }
            _name = value
        }

    internal var parentAddress: ParentGroupAddress = UnassignedAddress
        set(value) {
            require(value != address) {
                "Primary group address cannot be the same as the parent group address!"
            }
            MeshNetwork.onChange(oldValue = field, newValue = value) { network?.updateTimestamp() }
            field = value
        }

    @Transient
    var network: MeshNetwork? = null
        internal set

    var parent: Group?
        get() = when (parentAddress.address) {
            unassignedAddress -> null
            else -> network?.run { _groups.find { group -> group.address == parentAddress } }
        }
        set(value) {
            value?.let { group ->
                network?.run {
                    require(_groups.contains(group)) { throw DoesNotBelongToNetwork() }
                    parentAddress = toParentGroupAddress(group.address)
                }
            } ?: run {
                parentAddress = UnassignedAddress
            }
        }

    val isUsed: Boolean
        get() = network?.run {
            if (_groups.any { isDirectParentOf(it) }) return true
            _nodes.any { node ->
                node.elements.any { element ->
                    element.models.any { model ->
                        model.publish?.address == address ||
                                model.subscribe.contains(address as SubscriptionAddress)
                    }
                }
            }
        } ?: false

    /**
     * Returns whether this Group is a direct child group of the given one.
     *
     * @param parent: The Group to compare.
     * @return True if this Group is a child group of the given one, otherwise false.
     */
    fun isDirectChildOf(parent: Group) = this.parent == parent

    /**
     * Returns whether this Group is the parent group of the given one.
     *
     * @param child: The Group to compare.
     * @return True if the given Group is a child group of this one, otherwise false.
     */
    fun isDirectParentOf(child: Group) = child.isDirectChildOf(this)

    /**
     * Returns whether this Group is a child group of the given one.
     *
     * @param parent The Group to compare.
     * @return True if this Group is a child group of the given one, otherwise false.
     */
    fun isChildOf(parent: Group): Boolean = this.parent?.let { group ->
        group == parent || group.isChildOf(parent)
    } ?: false

    /**
     * Returns whether this Group is a parent group of the given one.
     *
     * @param child The Group to compare.
     * @return True if this Group is a parent group of the given one, otherwise false`.
     */
    fun isParentOf(child: Group) = child.isChildOf(this)

    /**
     * Sets the parent-child relationship between this and the given Group.
     *
     * @param parent The parent Group.
     */
    fun setAsChildOf(parent: Group) {
        require(parent != this) {
            throw IllegalArgumentException("A group cannot be a parent of itself")
        }
        parentAddress = toParentGroupAddress(parent.address)
    }

    /**
     * Sets the parent-child relationship between this and the given Group.
     *
     * @param child The child Group.
     */
    fun setAsParentOf(child: Group) {
        require(child != this) {
            throw IllegalArgumentException("A group cannot be a child of itself")
        }
        child.parentAddress = toParentGroupAddress(address)
    }

    /**
     * Returns a list of nodes with at least one model on any element subscribed to this group.
     */
    fun nodes(): List<Node> = network?._nodes?.filter { node ->
        node.elements.any { element ->
            element.models.any { model ->
                model.publish?.address == address ||
                        model.subscribe.contains(address as SubscriptionAddress)
            }
        }
    } ?: listOf()

    /**
     * Returns a list of elements with at least one model subscribed to this group.
     */
    fun elements(): List<Element> = network?._nodes?.flatMap { node ->
        node.elements.filter { element ->
            element.models.any { model ->
                model.publish?.address == address ||
                        model.subscribe.contains(address as SubscriptionAddress)
            }
        }
    } ?: listOf()

    private companion object {
        /**
         * Converts a primary group address to a parent group address.
         *
         * @param address Primary group address of the group.
         * @return a PrimaryGroupAddress as a ParentGroupAddress of type
         *         GroupAddress or VirtualAddress.
         */
        fun toParentGroupAddress(address: PrimaryGroupAddress): ParentGroupAddress =
            when (address) {
                is GroupAddress -> address
                is VirtualAddress -> address
            }
    }
}
