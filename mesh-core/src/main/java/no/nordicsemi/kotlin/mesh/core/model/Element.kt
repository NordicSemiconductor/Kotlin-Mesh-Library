package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import no.nordicsemi.kotlin.mesh.core.model.serialization.LocationAsStringSerializer

/**
 * Element represents a mesh element that is defined as an addressable entity within a mesh node.
 *
 * @property location    Describes the element location.
 * @property models      List of [Model] within an element.
 * @property name        A human-readable name that can identify an element within the node
 *                       and is optional according to Mesh CDB.
 * @property index       The index property contains an integer from 0 to 255 that represents
 *                       the numeric order of the element within this node and a node has at-least
 *                       one element which is called the primary element.
 * @property parentNode  Parent node that an element may belong to.
 */
@Serializable
data class Element internal constructor(
    @Serializable(with = LocationAsStringSerializer::class)
    val location: Location,
    val models: List<Model>
) {
    var name: String? = null
        set(value) {
            name?.let {
                require(it.isNotBlank()) { "Element name cannot be blank!" }
            }
            MeshNetwork.onChange(oldValue = field, newValue = value) {
                parentNode?.network?.updateTimestamp()
            }
            field = value
        }

    // Final index will be set when Element is added to the Node.
    // Refer https://github.com/NordicSemiconductor/IOS-nRF-Mesh-Library/blob/
    // c1755555f76fb6f393bfdad37a23566ddd581536/nRFMeshProvision/Classes/Mesh%20Model/Node.swift#L620
    var index: Int = models.size
        internal set

    @Transient
    internal var parentNode: Node? = null

    @Transient
    var unicastAddress =
        parentNode?._primaryUnicastAddress ?: UnicastAddress(address = models.size.toUShort())
        internal set

    init {
        require(index in LOWER_BOUND..HIGHER_BOUND) {
            " Index must be a value ranging from $LOWER_BOUND to $HIGHER_BOUND!"
        }
    }

    companion object {
        const val LOWER_BOUND = 0
        const val HIGHER_BOUND = 255
    }
}