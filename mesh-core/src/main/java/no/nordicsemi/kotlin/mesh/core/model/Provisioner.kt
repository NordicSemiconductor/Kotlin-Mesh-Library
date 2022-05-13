@file:Suppress("MemberVisibilityCanBePrivate", "PropertyName")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import no.nordicsemi.kotlin.mesh.core.model.serialization.UUIDSerializer
import java.util.*

/**
 * A Provisioner is capable of provisioning a device to a mesh network and is represented by a
 * provisioner object in the Mesh Configuration Database. A provisioner is represented as a node in
 * mesh network only if it is assigned a unicast address. Having a unicast address assigned allows
 * configuring nodes in the mesh network. Otherwise, a provisioner can only provision nodes to a
 * mesh network.
 *
 * @property name                      Provisioner name.
 * @property uuid                      UUID of the provisioner.
 * @property allocatedUnicastRanges    List of allocated unicast ranges for a given provisioner.
 * @property allocatedGroupRanges      List of allocated group ranges for a given provisioner.
 * @property allocatedSceneRanges      List of allocated scene ranges for a given provisioner.
 * @constructor Creates a Provisioner object.
 */
@Serializable
data class Provisioner internal constructor(
    @SerialName(value = "UUID")
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID,
    @SerialName(value = "allocatedUnicastRange")
    internal var _allocatedUnicastRanges: MutableList<UnicastRange> = mutableListOf(),
    @SerialName(value = "allocatedGroupRange")
    internal var _allocatedGroupRanges: MutableList<GroupRange> = mutableListOf(),
    @SerialName(value = "allocatedSceneRange")
    internal var _allocatedSceneRanges: MutableList<SceneRange> = mutableListOf()
) {

    constructor(uuid: UUID) : this(
        uuid,
        mutableListOf<UnicastRange>(),
        mutableListOf<GroupRange>(),
        mutableListOf<SceneRange>()
    )

    @SerialName(value = "provisionerName")
    var name: String = "nRF Mesh Provisioner"
        set(value) {
            require(value = value.isNotBlank()) { "Name cannot be empty!" }
            MeshNetwork.onChange(oldValue = field, newValue = value) { network?.updateTimestamp() }
            field = value
        }

    val allocatedUnicastRanges: List<UnicastRange>
        get() = _allocatedUnicastRanges

    val allocatedGroupRanges: List<GroupRange>
        get() = _allocatedGroupRanges

    val allocatedSceneRanges: List<SceneRange>
        get() = _allocatedSceneRanges

    val node: Node?
        get() = network?._nodes?.find { it.uuid == uuid }

    @Transient
    internal var network: MeshNetwork? = null

    /**
     * Allocates a given unicast range to a provisioner.
     *
     * @param range Allocated unicast range.
     */
    fun allocate(range: UnicastRange) {
        // TODO Check for overlapping ranges when allocating
        _allocatedUnicastRanges.add(range)
    }

    /**
     * Allocates the given group range to a provisioner.
     *
     * @param range Allocated group range.
     */
    fun allocate(range: GroupRange) {
        // TODO Check for overlapping ranges when allocating
        _allocatedGroupRanges.add(range)
    }

    /**
     * Allocates the given scene range to a provisioner.
     *
     * @param range Allocated scene range.
     */
    fun allocate(range: SceneRange) {
        // TODO Check for overlapping ranges when allocating
        _allocatedSceneRanges.add(range)
    }

    /**
     * Checks if the given range is within the Allocated range.
     *
     * @param range Range to be checked.
     * @return true if the range is within the allocated range.
     */
    fun hasAllocatedRange(range: Range) = when (range) {
        is UnicastRange -> _allocatedUnicastRanges
        is GroupRange -> _allocatedGroupRanges
        is SceneRange -> _allocatedSceneRanges
    }.contains(range)

    /**
     * Checks if the current provisioner has overlapping unicast, group or scene ranges with the
     * given provisioner.
     *
     * @param provisioner Other provisioner.
     * @return true if there are any overlapping unicast, groups or scene ranges or false otherwise.
     */
    fun hasOverlappingRanges(provisioner: Provisioner) =
        hasOverlappingUnicastRanges(other = provisioner) ||
                hasOverlappingGroupRanges(other = provisioner) ||
                hasOverlappingSceneRanges(other = provisioner)

    /**
     * Checks if the current provisioner has overlapping unicast ranges with the given provisioner.
     *
     * @param other Other provisioner.
     * @return true if there are any overlapping unicast ranges or false otherwise.
     */
    fun hasOverlappingUnicastRanges(other: Provisioner) = _allocatedUnicastRanges.any { range ->
        other._allocatedUnicastRanges.any { otherRange -> otherRange.overlaps(range) }
    }

    /**
     * Checks if the current provisioner has overlapping group ranges with the given provisioner.
     *
     * @param other Other provisioner.
     * @return true if there are any overlapping group ranges or false otherwise.
     */

    fun hasOverlappingGroupRanges(other: Provisioner) = _allocatedGroupRanges.any { range ->
        other._allocatedGroupRanges.any { otherRange -> otherRange.overlaps(range) }
    }

    /**
     * Checks if the current provisioner has overlapping scene ranges with the given provisioner.
     *
     * @param other Other provisioner.
     * @return true if there are any overlapping scene ranges or false otherwise.
     */
    fun hasOverlappingSceneRanges(other: Provisioner) = _allocatedSceneRanges.any { range ->
        other._allocatedSceneRanges.any { otherRange -> otherRange.overlaps(range) }
    }

}