package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable

/**
 * The AllocatedUnicastRange represents the range of unicast addresses that the Provisioner can allocate to
 * new devices when they are provisioned onto the mesh network, without needing to coordinate the node
 * additions with other Provisioners. The lowAddress and highAddress represent values from 0x0001 to 0x7FFF.
 * The value of the lowAddress property shall be less than or equal to the value of the highAddress property.
 *
 * @param lowAddress	Low address for a given range.
 * @param highAddress 	High address for a given  range.
 */
@Serializable
data class AllocatedUnicastRange(
    val lowAddress: Int,
    val highAddress: Int
)