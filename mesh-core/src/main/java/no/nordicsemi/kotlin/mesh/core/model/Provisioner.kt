@file:Suppress("MemberVisibilityCanBePrivate")

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
 */
@Serializable
data class Provisioner(
    @SerialName(value = "UUID")
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID
) {

    @SerialName(value = "provisionerName")
    var name: String = "nRF Mesh Provisioner"
        set(value) {
            require(value = value.isNotBlank()) { "Name cannot be empty!" }
            MeshNetwork.onChange(oldValue = field, newValue = value) { network?.updateTimestamp() }
            field = value
        }

    @SerialName(value = "allocatedUnicastRange")
    var allocatedUnicastRanges = listOf<UnicastRange>()
        private set

    @SerialName(value = "allocatedGroupRange")
    var allocatedGroupRanges = listOf<GroupRange>()
        private set

    @SerialName(value = "allocatedSceneRange")
    var allocatedSceneRanges = listOf<SceneRange>()
        private set

    val node: Node?
        get() = network?.nodes?.find { it.uuid == uuid }

    @Transient
    internal var network: MeshNetwork? = null

    /**
     * Allocates a given unicast range to a provisioner.
     *
     * @param range Allocated unicast range.
     */
    fun allocate(range: UnicastRange) {
        allocatedUnicastRanges = (allocatedUnicastRanges + range).map { it as UnicastRange }
    }

    /**
     * Allocates the given group range to a provisioner.
     *
     * @param range Allocated group range.
     */
    fun allocate(range: GroupRange) {
        allocatedGroupRanges = (allocatedGroupRanges + range).map { it as GroupRange }
    }

    /**
     * Allocates the given scene range to a provisioner.
     *
     * @param range Allocated scene range.
     */
    fun allocate(range: SceneRange) {
        allocatedSceneRanges = (allocatedSceneRanges + range).map { it as SceneRange }
    }

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
    fun hasOverlappingUnicastRanges(other: Provisioner) = allocatedUnicastRanges.any { range ->
        other.allocatedUnicastRanges.any { otherRange -> otherRange.overlaps(range) }
    }

    /**
     * Checks if the current provisioner has overlapping group ranges with the given provisioner.
     *
     * @param other Other provisioner.
     * @return true if there are any overlapping group ranges or false otherwise.
     */

    fun hasOverlappingGroupRanges(other: Provisioner) = allocatedGroupRanges.any { range ->
        other.allocatedGroupRanges.any { otherRange -> otherRange.overlaps(range) }
    }

    /**
     * Checks if the current provisioner has overlapping scene ranges with the given provisioner.
     *
     * @param other Other provisioner.
     * @return true if there are any overlapping scene ranges or false otherwise.
     */
    fun hasOverlappingSceneRanges(other: Provisioner) = allocatedSceneRanges.any { range ->
        other.allocatedSceneRanges.any { otherRange -> otherRange.overlaps(range) }
    }
}