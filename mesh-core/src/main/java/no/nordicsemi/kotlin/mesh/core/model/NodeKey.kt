@file:Suppress("MemberVisibilityCanBePrivate")

package no.nordicsemi.kotlin.mesh.core.model

/**
 * The state of a [NetworkKey] or [ApplicationKey] distributed to a mesh node by a Mesh Manager.
 *
 * @param index        Index of the [NetworkKey] or the [ApplicationKey].
 * @param updated        The updated property contains a Boolean value that is set to “false”, unless the Key Refresh procedure
 * 						is in progress and the [NetworkKey] or the [ApplicationKey] has been successfully updated.
 */
data class NodeKey internal constructor(
    val index: Int,
    val updated: Boolean
)