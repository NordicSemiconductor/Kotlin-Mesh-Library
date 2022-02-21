package no.nordicsemi.kotlin.mesh.core.model

/**
 * The AllocatedGroupRange represents the range of group addresses that the Provisioner can allocate to
 * newly created groups, without needing to coordinate the group additions with other Provisioners.
 * The lowAddress and highAddress properties represent values from 0xC000 to 0xFFFF. The value of the
 * lowAddress property shall be less than or equal to the value of the highAddress property.
 *
 * @param lowAddress    Low address for a given range.
 * @param highAddress	High address for a given  range.
 */
data class AllocatedGroupRange(
    val lowAddress: Int,
    val highAddress: Int
)