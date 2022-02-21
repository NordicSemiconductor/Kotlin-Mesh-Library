package no.nordicsemi.kotlin.mesh.core.model

/**
 * Scene
 *
 * @param name          Scene name.
 * @param number        Scene number.
 * @param addresses     Addresses containing the scene.
 */
data class Scene(
    val name: String,
    val number: Int,
    val addresses: List<Int>
) {
    init {
        require(name.isNotBlank()) { "Scene name cannot be blank!" }
        require(number in LOWER_BOUND..HIGHER_BOUND) { "Scene number must be within 0xC000 and 0xFFFF!" }
    }

    companion object {
        const val LOWER_BOUND = 0x0001
        const val HIGHER_BOUND = 0xFFFF
    }
}