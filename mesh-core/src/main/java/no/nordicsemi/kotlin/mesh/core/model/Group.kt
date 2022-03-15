package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable

/**
 * Group
 *
 * @param name             Group name.
 * @param address          Address of the group.
 * @param parentAddress    Parent address of the group if the given group is a sub group.
 */
@Serializable
data class Group(
    val name: String,
    val address: PrimaryGroupAddress,
    val parentAddress: ParentGroupAddress = UnassignedAddress
) {
    init {
        require(name.isNotBlank()) { "Group name cannot be blank!" }
        require(address.address != parentAddress.address) { "Primary group address cannot be the same as the parent group address!" }
    }
}
