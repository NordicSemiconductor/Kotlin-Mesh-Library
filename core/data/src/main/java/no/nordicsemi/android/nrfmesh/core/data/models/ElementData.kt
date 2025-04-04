@file:Suppress("MemberVisibilityCanBePrivate")

package no.nordicsemi.android.nrfmesh.core.data.models

import no.nordicsemi.kotlin.mesh.core.model.Element
import no.nordicsemi.kotlin.mesh.core.model.Location
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress

/**
 * Element represents a mesh element that is defined as an addressable entity within a mesh node.
 *
 * @property location           Describes the element location.
 * @property models             List of [Model] within an element.
 * @property name               A human-readable name that can identify an element within the node
 *                              and is optional according to Mesh CDB.
 * @property index              The index property contains an integer from 0 to 255 that represents
 *                              the numeric order of the element within this node and a node has
 *                              at-least one element which is called the primary element.
 * @property parentNode         Parent node that an element may belong to.
 * @property unicastAddress     Address of the element.
 * @constructor Creates an Element object.
 */
data class ElementData(
    val name: String?,
    val index: Int,
    val location: Location,
    val models: List<ModelData>,
    val unicastAddress: UnicastAddress,
    val isPrimary: Boolean
) {

    /**
     * Convenience constructor to create an ElementData object from an [Element] object.
     *
     * @param element Element object.
     */
    constructor(element: Element) : this(
        name = element.name,
        index = element.index,
        location = element.location,
        models = element.models.map { ModelData(it) },
        unicastAddress = element.unicastAddress,
        isPrimary = element.isPrimary
    )
}