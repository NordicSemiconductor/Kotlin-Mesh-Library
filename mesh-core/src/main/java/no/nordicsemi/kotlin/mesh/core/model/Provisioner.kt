package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nordicsemi.kotlin.mesh.core.model.serialization.UuidSerializer
import java.util.*

/**
 * A Provisioner is a mesh node that is capable of provisioning a device to the mesh network and,
 * is represented by a provisioner object in the Mesh Configuration Database.
 *
 * @param name                      Provisioner name.
 * @param uuid                      UUID of the provisioner.
 * @param allocatedUnicastRanges    List of allocated unicast ranges for a given provisioner.
 * @param allocatedGroupRanges      List of allocated group ranges for a given provisioner.
 * @param allocatedSceneRanges      List of allocated scene ranges for a given provisioner.
 */
@Serializable
data class Provisioner(
    @SerialName("provisionerName")
    val name: String,
    @Serializable(with = UuidSerializer::class)
    val uuid: UUID,
    val allocatedUnicastRanges: List<AllocatedUnicastRange>,
    val allocatedGroupRanges: List<AllocatedGroupRange>,
    val allocatedSceneRanges: List<AllocatedSceneRange>
)