package no.nordicsemi.kotlin.mesh.core.model

/**
 * Group
 *
 * @param name             Group name.
 * @param address          Address of the group.
 * @param parentAddress    Parent address of the group if the given group is a sub group.
 */
data class Group(
    val name: String,
    val address: Int,
    val parentAddress: Int = 0x0000
) {
    init {
        require(name.isNotBlank()) { "Group name cannot be blank!" }
        require(address in LOWER_BOUND..HIGHER_BOUND) { "Group address must be within 0xC000 and 0xFFFF!" }
        require(parentAddress != 0x0000 && parentAddress in LOWER_BOUND..HIGHER_BOUND) { "Group address must be within 0xC000 and 0xFFFF!" }
    }

    companion object {
        const val LOWER_BOUND = 0xC000
        const val HIGHER_BOUND = 0xFFFF
    }
}