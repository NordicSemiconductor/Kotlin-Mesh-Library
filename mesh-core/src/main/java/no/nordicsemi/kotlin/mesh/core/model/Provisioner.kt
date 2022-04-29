package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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

    internal var network: MeshNetwork? = null

    /**
     * Allocates a given unicast range to a provisioner.
     *
     * @param range Allocated unicast range.
     */
    fun allocate(range: UnicastRange) {
        // TODO Merge
        allocatedUnicastRanges = (allocatedUnicastRanges + range).map { it as UnicastRange }
    }

    /**
     * Allocates the given group range to a provisioner.
     *
     * @param range Allocated group range.
     */
    fun allocate(range: GroupRange) {
        // TODO Merge
        allocatedGroupRanges = (allocatedGroupRanges + range).map { it as GroupRange }
    }

    /**
     * Allocates the given scene range to a provisioner.
     *
     * @param range Allocated scene range.
     */
    fun allocate(range: SceneRange) {
        // TODO Merge
        allocatedSceneRanges = (allocatedSceneRanges + range).map { it as SceneRange }
    }
}