package no.nordicsemi.kotlin.mesh.core.model

/**
 * The AllocatedSceneRange represents the range of scene numbers that the Provisioner can use to register
 * new scenes in the mesh network, without needing to coordinate the allocated scene numbers with other
 * Provisioners. The firstScene and lastScene represents values from 0x0001 to 0xFFFF. The value of the
 * firstScene property shall be less than or equal to the value of the lastScene property.
 *
 * @param lowAddress    Low address for a given range.
 * @param highAddress   High address for a given  range.
 */
data class AllocatedSceneRange(
    val lowAddress: Int,
    val highAddress: Int
)