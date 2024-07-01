package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The state of a [NetworkKey] or [ApplicationKey] distributed to a mesh node by a Mesh Manager.
 *
 * @property index         Index of the [NetworkKey] or the [ApplicationKey].
 * @property _updated      Updated property contains a Boolean value that is set to false, unless
 *                         the Key Refresh procedure is in progress and the [NetworkKey] or the
 *                         [ApplicationKey] has been successfully updated.
 */
@Serializable
data class NodeKey internal constructor(
    val index: KeyIndex,
    @SerialName("updated")
    private var _updated: Boolean
) {
    val updated: Boolean
        get() = _updated

    internal constructor(key: NetworkKey) : this(
        index = key.index,
        _updated = key.phase != NormalOperation
    )

    internal constructor(key: ApplicationKey) : this(
        index = key.index,
        _updated = key.boundNetworkKey?.phase != NormalOperation
    )

    /**
     * Marks the key as updated.
     *
     * @param updated Updated flag.
     */
    internal fun update(updated: Boolean) {
        _updated = updated
    }
}

/**
 * Returns an Node Key with the given KeyIndex.
 *
 * @param index Key Index.
 * @return Node key.
 */
infix fun List<NodeKey>.get(index: KeyIndex): NodeKey? = find { it.index == index }