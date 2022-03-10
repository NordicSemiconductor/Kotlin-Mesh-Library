package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Contextual
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
    @Contextual
    val address: GroupAddress,
    @Contextual
    val parentAddress: GroupAddress = GroupAddress(address = 0x0000u)
) {
    init {
        require(name.isNotBlank()) { "Group name cannot be blank!" }
        require(address.address in LOWER_BOUND..HIGHER_BOUND) { "Group address must be within 0xC000 and 0xFFFF!" }
        require(parentAddress.address.toInt() == 0x0000 && parentAddress.address in LOWER_BOUND..HIGHER_BOUND) { "Group address must be within 0xC000 and 0xFFFF!" }
    }

    companion object {
        const val LOWER_BOUND = 0xC000u
        const val HIGHER_BOUND = 0xFEFFu
    }
}