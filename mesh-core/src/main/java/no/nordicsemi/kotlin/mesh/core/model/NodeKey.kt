@file:Suppress("MemberVisibilityCanBePrivate")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable

/**
 * The state of a [NetworkKey] or [ApplicationKey] distributed to a mesh node by a Mesh Manager.
 *
 * @property index         Index of the [NetworkKey] or the [ApplicationKey].
 * @property updated       The updated property contains a Boolean value that is set to “false”, unless the Key Refresh procedure
 * 					    is in progress and the [NetworkKey] or the [ApplicationKey] has been successfully updated.
 */
@Serializable
data class NodeKey internal constructor(
    val index: Int,
    val updated: Boolean
)