package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable

/**
 * Element represents a mesh element that is defined as an addressable entity within a mesh node.
 *
 * @property name           A human-readable name that can identify an element within the node.
 * @property index          The index property contains an integer from 0 to 255 that represents the numeric order of
 * 					        the element within this node and a node has at-least one element which is called the primary element.
 * @property location       Describes the element location.
 * @property models         List of [Model] within an element.
 */
@Serializable
data class Element internal constructor(
    val name: String,
    val index: Int,
    val location: Int,
    val models: List<Model>
) {
    init {
        require(name.isNotBlank()) { "Element name cannot be blank!" }
        require(index in LOWER_BOUND..HIGHER_BOUND) { " Index must be a value ranging from 0 to 256!" }
    }

    companion object {
        const val LOWER_BOUND = 0
        const val HIGHER_BOUND = 256
    }
}