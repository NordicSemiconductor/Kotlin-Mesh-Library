@file:Suppress("unused", "EXPERIMENTAL_API_USAGE")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable

/**
 *  Allocated Range.
 *
 * @property low       Low value for a given range.
 * @property high      High value for a given  range.
 */
// TODO Check IntRange
@Serializable
sealed class AllocatedRange(val low: UShort, val high: UShort)

/**
 * Allocated address range
 *
 * @property lowAddress       Low value for a given range.
 * @property highAddress      High value for a given  range.
 */
@Serializable
sealed class AllocatedAddressRange {
    abstract val lowAddress: MeshAddress
    abstract val highAddress: MeshAddress
}

/**
 * The AllocatedUnicastRange represents the range of unicast addresses that the Provisioner can allocate to
 * new devices when they are provisioned onto the mesh network, without needing to coordinate the node
 * additions with other Provisioners. The lowAddress and highAddress represent values from 0x0001 to 0x7FFF.
 * The value of the lowAddress property shall be less than or equal to the value of the highAddress property.
 *
 * @property lowAddress        Low address for a given range.
 * @property highAddress       High address for a given  range.
 */
@Serializable
data class AllocatedUnicastRange(
    override val lowAddress: UnicastAddress,
    override val highAddress: UnicastAddress
) : AllocatedAddressRange()

/**
 * The AllocatedGroupRange represents the range of group addresses that the Provisioner can allocate to
 * newly created groups, without needing to coordinate the group additions with other Provisioners.
 * The lowAddress and highAddress properties represent values from 0xC000 to 0xFFFF. The value of the
 * lowAddress property shall be less than or equal to the value of the highAddress property.
 *
 * @property lowAddress        Low address for a given range.
 * @property highAddress       High address for a given  range.
 */
@Serializable
data class AllocatedGroupRange(
    override val lowAddress: GroupAddress,
    override val highAddress: GroupAddress
) : AllocatedAddressRange()

/**
 * The AllocatedSceneRange represents the range of scene numbers that the Provisioner can use to register
 * new scenes in the mesh network, without needing to coordinate the allocated scene numbers with other
 * Provisioners. The firstScene and lastScene represents values from 0x0001 to 0xFFFF. The value of the
 * firstScene property shall be less than or equal to the value of the lastScene property.
 *
 * @property firstScene    First scene a given range.
 * @property lastScene     Last scene for a given  range.
 */
@Serializable
data class AllocatedSceneRange(
    val firstScene: SceneNumber,
    val lastScene: SceneNumber
) //: AllocatedRange(low = firstScene, high = lastScene)