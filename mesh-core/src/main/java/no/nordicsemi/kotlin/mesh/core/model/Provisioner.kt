package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nordicsemi.kotlin.mesh.core.model.serialization.UUIDSerializer
import java.util.*

/**
 * A Provisioner is a mesh node that is capable of provisioning a device to the mesh network and,
 * is represented by a provisioner object in the Mesh Configuration Database.
 *
 * @property name                      Provisioner name.
 * @property uuid                      UUID of the provisioner.
 * @property allocatedUnicastRanges    List of allocated unicast ranges for a given provisioner.
 * @property allocatedGroupRanges      List of allocated group ranges for a given provisioner.
 * @property allocatedSceneRanges      List of allocated scene ranges for a given provisioner.
 */
@Serializable
data class Provisioner(
    @SerialName(value = "provisionerName")
    val name: String,
    @SerialName(value = "UUID")
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID
) {
    @SerialName(value = "allocatedUnicastRange")
    var allocatedUnicastRanges = listOf<AllocatedUnicastRange>()
        private set

    @SerialName(value = "allocatedGroupRange")
    var allocatedGroupRanges = listOf<AllocatedGroupRange>()
        private set

    @SerialName(value = "allocatedSceneRange")
    var allocatedSceneRanges = listOf<AllocatedSceneRange>()
        private set

    /**
     * Adds an allocated unicast range to a provisioner.
     *
     * @param range Allocated unicast range.
     */
    fun addAllocatedUnicastRange(range: AllocatedUnicastRange) {
        this.allocatedUnicastRanges += range
    }

    /**
     * Adds an allocated unicast group to a provisioner.
     *
     * @param range Allocated group range.
     */
    fun addAllocatedGroupRange(range: AllocatedGroupRange) {
        this.allocatedGroupRanges += range
    }

    /**
     * Adds an allocated scene range to a provisioner.
     *
     * @param range Allocated scene range.
     */
    fun addAllocatedSceneRange(range: AllocatedSceneRange) {
        this.allocatedSceneRanges += range
    }
}