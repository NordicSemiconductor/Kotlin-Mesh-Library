package no.nordicsemi.kotlin.mesh.core.model

/**
 * Element represents a mesh element that is defined as an addressable entity within a mesh node.
 *
 * @param name         A human-readable name that can identify an element within the node.
 * @param index        The index property contains an integer from 0 to 255 that represents the numeric order of
 * 					   the element within this node and a node has at-least one element which is called the primary element.
 * @param location     Describes the element location.
 * @param models       Array of [Model] within an element.
 */
data class Element(
    val name: String,
    val index: Int,
    val location: Int,
    val models: Array<Model>
) {
    init {
        require(name.isNotBlank()) { "Element name cannot be blank!" }
        require(index in LOWER_BOUND..HIGHER_BOUND) { " Index must be a value ranging from 0 to 256!" }
    }

    companion object {
        const val LOWER_BOUND = 0
        const val HIGHER_BOUND = 256
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Element

        if (name != other.name) return false
        if (index != other.index) return false
        if (location != other.location) return false
        if (!models.contentEquals(other.models)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + index
        result = 31 * result + location
        result = 31 * result + models.contentHashCode()
        return result
    }
}