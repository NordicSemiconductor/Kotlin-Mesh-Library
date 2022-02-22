package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.SerialName
import java.util.*

/**
 * A Provisioner is a mesh node that is capable of provisioning a device to the mesh network and,
 * is represented by a provisioner object in the Mesh Configuration Database
 *
 * @param name                      Provisioner name.
 * @param uuid                      UUID of the provisioner.
 * @param allocatedUnicastRanges    List of allocated unicast ranges for a given provisioner.
 * @param allocatedGroupRanges      List of allocated group ranges for a given provisioner.
 * @param allocatedSceneRanges      List of allocated scene ranges for a given provisioner.
 */
data class Provisioner(
    @SerialName("provisionerName")
    val name: String,
    val uuid: UUID,
    val allocatedUnicastRanges: Array<AllocatedUnicastRange>,
    val allocatedGroupRanges: Array<AllocatedGroupRange>,
    val allocatedSceneRanges: Array<AllocatedSceneRange>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Provisioner

        if (name != other.name) return false
        if (uuid != other.uuid) return false
        if (!allocatedUnicastRanges.contentEquals(other.allocatedUnicastRanges)) return false
        if (!allocatedGroupRanges.contentEquals(other.allocatedGroupRanges)) return false
        if (!allocatedSceneRanges.contentEquals(other.allocatedSceneRanges)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + uuid.hashCode()
        result = 31 * result + allocatedUnicastRanges.contentHashCode()
        result = 31 * result + allocatedGroupRanges.contentHashCode()
        result = 31 * result + allocatedSceneRanges.contentHashCode()
        return result
    }
}